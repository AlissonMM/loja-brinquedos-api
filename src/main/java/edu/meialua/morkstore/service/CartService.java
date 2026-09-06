package edu.meialua.morkstore.service;

import edu.meialua.morkstore.adapters.in.Cart;
import edu.meialua.morkstore.adapters.in.CartItem;
import edu.meialua.morkstore.adapters.in.Product;
import edu.meialua.morkstore.adapters.in.User;
import edu.meialua.morkstore.adapters.in.repositories.CartItemRepository;
import edu.meialua.morkstore.adapters.in.repositories.CartRepository;
import edu.meialua.morkstore.adapters.in.repositories.ProductRepository;
import edu.meialua.morkstore.exception.ResourceNotFoundException;
import edu.meialua.morkstore.model.CartResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
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
    public CartResponseDTO addItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCartEntity(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
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
