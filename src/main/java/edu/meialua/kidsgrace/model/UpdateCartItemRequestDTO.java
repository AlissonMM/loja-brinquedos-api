package edu.meialua.kidsgrace.model;

import jakarta.validation.constraints.Min;

public class UpdateCartItemRequestDTO {

    @Min(value = 1, message = "quantity deve ser no mínimo 1")
    private int quantity;

    public UpdateCartItemRequestDTO() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
