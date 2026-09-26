package edu.meialua.morkstore.model;

import edu.meialua.morkstore.adapters.in.CartItem;

import java.util.Base64;

public class CartItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private float unitPrice;
    private int quantity;
    private float subtotal;
    private String image;

    public CartItemResponseDTO() {
    }

    public CartItemResponseDTO(CartItem item) {
        this.id = item.getId();
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.unitPrice = item.getProduct().getValue();
        this.quantity = item.getQuantity();
        this.subtotal = this.unitPrice * this.quantity;
        this.image = item.getProduct().getImage() != null
                ? Base64.getEncoder().encodeToString(item.getProduct().getImage())
                : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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
