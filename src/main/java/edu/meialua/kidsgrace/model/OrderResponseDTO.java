package edu.meialua.kidsgrace.model;

import edu.meialua.kidsgrace.adapters.in.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private String status;
    private List<OrderItemResponseDTO> items;
    private float totalValue;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.status = order.getStatus().name();
        this.items = order.getItems().stream()
                .map(OrderItemResponseDTO::new)
                .collect(Collectors.toList());
        this.totalValue = order.getTotalValue();
        this.createdAt = order.getCreatedAt();
        this.paidAt = order.getPaidAt();
        this.cancelledAt = order.getCancelledAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDTO> items) {
        this.items = items;
    }

    public float getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(float totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
