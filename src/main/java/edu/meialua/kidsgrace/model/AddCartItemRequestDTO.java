package edu.meialua.kidsgrace.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequestDTO {

    @NotNull(message = "toyId é obrigatório")
    private Long toyId;

    @Min(value = 1, message = "quantity deve ser no mínimo 1")
    private int quantity;

    public AddCartItemRequestDTO() {
    }

    public Long getToyId() {
        return toyId;
    }

    public void setToyId(Long toyId) {
        this.toyId = toyId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
