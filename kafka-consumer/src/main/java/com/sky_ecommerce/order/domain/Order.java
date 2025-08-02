package com.sky_ecommerce.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 100)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<LineItem> items = new ArrayList<>();

    public enum Status { PENDING, PAID, CANCELLED, FAILED }

    @Embeddable
    public static class LineItem {
        @Column(nullable = false, length = 100)
        private String sku;
        @Column(nullable = false)
        private int quantity;
        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal price;

        public String getSku() { return sku; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
        public void setSku(String sku) { this.sku = sku; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getTotal() { return total; }
    public Status getStatus() { return status; }
    public List<LineItem> getItems() { return items; }
    public void setId(String id) { this.id = id; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setStatus(Status status) { this.status = status; }
    public void setItems(List<LineItem> items) { this.items = items; }
}
