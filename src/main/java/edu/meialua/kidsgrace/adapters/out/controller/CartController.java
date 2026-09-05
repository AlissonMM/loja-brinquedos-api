package edu.meialua.kidsgrace.adapters.out.controller;

import edu.meialua.kidsgrace.adapters.in.User;
import edu.meialua.kidsgrace.model.AddCartItemRequestDTO;
import edu.meialua.kidsgrace.model.CartResponseDTO;
import edu.meialua.kidsgrace.model.UpdateCartItemRequestDTO;
import edu.meialua.kidsgrace.service.CartService;
import edu.meialua.kidsgrace.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Nenhum endpoint recebe userId/cartId na URL — o carrinho é sempre resolvido
 * a partir do usuário autenticado (CurrentUserService), o que evita qualquer
 * possibilidade de um usuário acessar o carrinho de outro.
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(@Valid @RequestBody AddCartItemRequestDTO request) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.addItem(user, request.getToyId(), request.getQuantity()));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {

        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.updateItem(user, itemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> removeItem(@PathVariable Long itemId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        User user = currentUserService.getCurrentUser();
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}
