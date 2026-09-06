package edu.meialua.morkstore.adapters.in;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Snapshot: preserva o nome/preço no momento da compra, mesmo que o
    // produto mude (ou seja removido) depois.
    @Column(name = "product_name_snapshot", length = 250, nullable = false)
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false)
    private float unitPriceSnapshot;

    @Column(name = "category_snapshot", length = 250)
    private String categorySnapshot;

    @Column(name = "brand_snapshot", length = 100)
    private String brandSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public float getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(float unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public String getCategorySnapshot() {
        return categorySnapshot;
    }

    public void setCategorySnapshot(String categorySnapshot) {
        this.categorySnapshot = categorySnapshot;
    }

    public String getBrandSnapshot() {
        return brandSnapshot;
    }

    public void setBrandSnapshot(String brandSnapshot) {
        this.brandSnapshot = brandSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getSubtotal() {
        return unitPriceSnapshot * quantity;
    }
}
