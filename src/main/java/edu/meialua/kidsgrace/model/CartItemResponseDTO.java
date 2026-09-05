package edu.meialua.kidsgrace.model;

import edu.meialua.kidsgrace.adapters.in.CartItem;

import java.util.Base64;

public class CartItemResponseDTO {

    private Long id;
    private Long toyId;
    private String toyName;
    private float unitPrice;
    private int quantity;
    private float subtotal;
    private String image;

    public CartItemResponseDTO() {
    }

    public CartItemResponseDTO(CartItem item) {
        this.id = item.getId();
        this.toyId = item.getToy().getId();
        this.toyName = item.getToy().getName();
        this.unitPrice = item.getToy().getValue();
        this.quantity = item.getQuantity();
        this.subtotal = this.unitPrice * this.quantity;
        this.image = item.getToy().getImage() != null
                ? Base64.getEncoder().encodeToString(item.getToy().getImage())
                : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getToyId() {
        return toyId;
    }

    public void setToyId(Long toyId) {
        this.toyId = toyId;
    }

    public String getToyName() {
        return toyName;
    }

    public void setToyName(String toyName) {
        this.toyName = toyName;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
