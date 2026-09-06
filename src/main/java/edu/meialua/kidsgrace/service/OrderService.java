package edu.meialua.kidsgrace.service;

import edu.meialua.kidsgrace.adapters.in.Cart;
import edu.meialua.kidsgrace.adapters.in.CartItem;
import edu.meialua.kidsgrace.adapters.in.Order;
import edu.meialua.kidsgrace.adapters.in.OrderItem;
import edu.meialua.kidsgrace.adapters.in.Toy;
import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.adapters.in.enums.Action;
import edu.meialua.kidsgrace.adapters.in.enums.EntityType;
import edu.meialua.kidsgrace.adapters.in.enums.OrderStatus;
import edu.meialua.kidsgrace.adapters.in.repositories.OrderRepository;
import edu.meialua.kidsgrace.adapters.in.repositories.ToyRepository;
import edu.meialua.kidsgrace.exception.EmptyCartException;
import edu.meialua.kidsgrace.exception.ForbiddenOperationException;
import edu.meialua.kidsgrace.exception.InsufficientStockException;
import edu.meialua.kidsgrace.exception.InvalidOrderStatusTransitionException;
import edu.meialua.kidsgrace.exception.ResourceNotFoundException;
import edu.meialua.kidsgrace.model.LogEvent;
import edu.meialua.kidsgrace.model.OrderResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ToyRepository toyRepository;
    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final KafkaProducerService kafkaProducerService;

    public OrderService(
            OrderRepository orderRepository,
            ToyRepository toyRepository,
            CartService cartService,
            CurrentUserService currentUserService,
            KafkaProducerService kafkaProducerService
    ) {
        this.orderRepository = orderRepository;
        this.toyRepository = toyRepository;
        this.cartService = cartService;
        this.currentUserService = currentUserService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public OrderResponseDTO checkout(User user) {
        Cart cart = cartService.getOrCreateCartEntity(user);

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Carrinho vazio — adicione itens antes de finalizar a compra.");
        }

        // Valida estoque de TODOS os itens antes de decrementar qualquer um,
        // para não deixar o carrinho parcialmente processado em caso de erro.
        for (CartItem cartItem : cart.getItems()) {
            Toy toy = cartItem.getToy();
            if (cartItem.getQuantity() > toy.getStock()) {
                throw new InsufficientStockException(String.format(
                        "Estoque insuficiente para \"%s\" (disponível: %d, solicitado: %d).",
                        toy.getName(), toy.getStock(), cartItem.getQuantity()
                ));
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0f;

        for (CartItem cartItem : cart.getItems()) {
            Toy toy = cartItem.getToy();

            toy.setStock(toy.getStock() - cartItem.getQuantity());
            toyRepository.save(toy);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setToy(toy);
            orderItem.setToyNameSnapshot(toy.getName());
            orderItem.setUnitPriceSnapshot(toy.getValue());
            orderItem.setCategorySnapshot(toy.getCategory());
            orderItem.setBrandSnapshot(toy.getBrand());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            total += toy.getValue() * cartItem.getQuantity();
        }

        order.setItems(orderItems);
        order.setTotalValue(total);
        order = orderRepository.save(order);

        cartService.clear(cart);

        publishOrderEvent(Action.ORDER_CREATED, order, user, String.format(
                "Pedido #%d criado com %d itens, total R$ %.2f",
                order.getId(), order.getItems().size(), order.getTotalValue()
        ));

        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO confirmPayment(User user, Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Esse pedido não pertence a você.");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusTransitionException("Só é possível confirmar pagamento de pedidos pendentes.");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        publishOrderEvent(Action.ORDER_PAID, order, user,
                String.format("Pedido #%d pago com sucesso.", order.getId()));

        // Só conta como "venda" para as métricas quando o pagamento é
        // confirmado — um pedido criado e depois cancelado nunca chega aqui.
        // Um evento por item (não um só por pedido) dá granularidade de
        // categoria/marca/produto para a analytics.
        for (OrderItem item : order.getItems()) {
            publishSaleEvent(order, item, user);
        }

        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(User user, Long orderId) {
        Order order = getOrderOrThrow(orderId);

        boolean isAdmin = currentUserService.isAdmin();
        boolean isOwner = order.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenOperationException("Esse pedido não pertence a você.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusTransitionException("Esse pedido já está cancelado.");
        }
        if (!isAdmin && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusTransitionException("Só é possível cancelar pedidos pendentes de pagamento.");
        }

        // Devolve o estoque reservado no checkout, independente do status
        // (cobre tanto um PENDING_PAYMENT quanto um PAID cancelado por admin).
        for (OrderItem item : order.getItems()) {
            Toy toy = item.getToy();
            toy.setStock(toy.getStock() + item.getQuantity());
            toyRepository.save(toy);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        String canceledBy = isOwner ? "cliente" : "administrador";
        publishOrderEvent(Action.ORDER_CANCELLED, order, user,
                String.format("Pedido #%d cancelado por %s.", order.getId(), canceledBy));

        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO getById(User user, Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (!currentUserService.isAdmin() && !order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Esse pedido não pertence a você.");
        }

        return new OrderResponseDTO(order);
    }

    @Transactional
    public List<OrderResponseDTO> listMine(User user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrderResponseDTO> listAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));
    }

    private void publishOrderEvent(Action action, Order order, User user, String description) {
        LogEvent event = LogEvent.builder()
                .action(action)
                .entity(EntityType.ORDER)
                .entityId(order.getId())
                .user(user.getUserName())
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducerService.sendOrderEvent(event);
    }

    // Evento granular por item vendido — entity/entityId apontam para o
    // brinquedo (não para o pedido), com categoria/marca/preço/quantidade
    // do momento da venda (snapshot), permitindo à analytics cortar receita
    // e "top produtos" sem depender do estado atual do catálogo.
    private void publishSaleEvent(Order order, OrderItem item, User user) {
        LogEvent event = LogEvent.builder()
                .action(Action.SALE)
                .entity(EntityType.PRODUCT)
                .entityId(item.getToy().getId())
                .user(user.getUserName())
                .description(String.format(
                        "Venda: %d un. de \"%s\" no pedido #%d.",
                        item.getQuantity(), item.getToyNameSnapshot(), order.getId()
                ))
                .category(item.getCategorySnapshot())
                .brand(item.getBrandSnapshot())
                .unitValue(item.getUnitPriceSnapshot())
                .quantity(item.getQuantity())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaProducerService.sendOrderEvent(event);
    }
}
