package edu.meialua.kidsgrace.model;

import edu.meialua.kidsgrace.adapters.in.Cart;

import java.util.List;
import java.util.stream.Collectors;

public class CartResponseDTO {

    private Long id;
    private List<CartItemResponseDTO> items;
    private float totalValue;

    public CartResponseDTO() {
    }

    public CartResponseDTO(Cart cart) {
        this.id = cart.getId();
        this.items = cart.getItems().stream()
                .map(CartItemResponseDTO::new)
                .collect(Collectors.toList());
        this.totalValue = (float) this.items.stream()
                .mapToDouble(CartItemResponseDTO::getSubtotal)
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponseDTO> items) {
        this.items = items;
    }

    public float getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(float totalValue) {
        this.totalValue = totalValue;
    }
}
