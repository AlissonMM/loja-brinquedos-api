package edu.meialua.morkstore.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequestDTO {

    @NotNull(message = "productId é obrigatório")
    private Long productId;

    @Min(value = 1, message = "quantity deve ser no mínimo 1")
    private int quantity;

    public AddCartItemRequestDTO() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
