package edu.meialua.kidsgrace.adapters.out.controller;

import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.model.OrderResponseDTO;
import edu.meialua.kidsgrace.service.CurrentUserService;
import edu.meialua.kidsgrace.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.checkout(user));
    }

    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderResponseDTO> confirmPayment(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.confirmPayment(user, id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.cancelOrder(user, id));
    }

    // /orders/mine antes de /orders/{id} — o Spring resolve o segmento
    // literal com prioridade, mas manter a ordem no arquivo ajuda a leitura.
    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponseDTO>> listMine() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.listMine(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(orderService.getById(user, id));
    }

    // ADMIN — todos os pedidos de todos os usuários (ver SecurityConfig).
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listAll() {
        return ResponseEntity.ok(orderService.listAll());
    }
}
