package edu.meialua.kidsgrace.service;

import edu.meialua.kidsgrace.adapters.in.Cart;
import edu.meialua.kidsgrace.adapters.in.CartItem;
import edu.meialua.kidsgrace.adapters.in.Toy;
import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.adapters.in.repositories.CartItemRepository;
import edu.meialua.kidsgrace.adapters.in.repositories.CartRepository;
import edu.meialua.kidsgrace.adapters.in.repositories.ToyRepository;
import edu.meialua.kidsgrace.exception.ResourceNotFoundException;
import edu.meialua.kidsgrace.model.CartResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ToyRepository toyRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ToyRepository toyRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.toyRepository = toyRepository;
    }

    /**
     * Busca o carrinho do usuário, criando um vazio se ainda não existir.
     * Usado tanto pelo CartController quanto pelo OrderService (checkout lê
     * os itens direto da entidade, não do DTO).
     */
    @Transactional
    public Cart getOrCreateCartEntity(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    LocalDateTime now = LocalDateTime.now();
                    cart.setCreatedAt(now);
                    cart.setUpdatedAt(now);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartResponseDTO getCart(User user) {
        return new CartResponseDTO(getOrCreateCartEntity(user));
    }

    @Transactional
    public CartResponseDTO addItem(User user, Long toyId, int quantity) {
        Cart cart = getOrCreateCartEntity(user);

        Toy toy = toyRepository.findById(toyId)
                .orElseThrow(() -> new ResourceNotFoundException("Brinquedo não encontrado."));

        CartItem item = cartItemRepository.findByCartIdAndToyId(cart.getId(), toyId)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setToy(toy);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        cartItemRepository.save(item);
        touch(cart);

        return new CartResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO updateItem(User user, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCartEntity(user);
        CartItem item = findOwnedItem(cart, cartItemId);

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        touch(cart);

        return new CartResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeItem(User user, Long cartItemId) {
        Cart cart = getOrCreateCartEntity(user);
        CartItem item = findOwnedItem(cart, cartItemId);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        touch(cart);

        return new CartResponseDTO(cart);
    }

    @Transactional
    public void clearCart(User user) {
        clear(getOrCreateCartEntity(user));
    }

    /**
     * Esvazia um carrinho já carregado (orphanRemoval cuida do delete das
     * linhas de cart_items). Reaproveitado pelo OrderService após o checkout.
     */
    @Transactional
    public void clear(Cart cart) {
        cart.getItems().clear();
        touch(cart);
    }

    private CartItem findOwnedItem(Cart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado no carrinho."));
    }

    private void touch(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
