package com.sky_ecommerce.cart.domain;

import com.sky_ecommerce.cart.domain.Cart;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_items_product", columnList = "productId", unique = false)
})
public class CartItem {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO; // price snapshot

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public CartItem setId(String id) { this.id = id; return this; }

    public Cart getCart() { return cart; }
    public CartItem setCart(Cart cart) { this.cart = cart; return this; }

    public String getProductId() { return productId; }
    public CartItem setProductId(String productId) { this.productId = productId; return this; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public CartItem setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }

    public Integer getQuantity() { return quantity; }
    public CartItem setQuantity(Integer quantity) { this.quantity = quantity; return this; }

    public Instant getCreatedAt() { return createdAt; }
    public CartItem setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }

    public Instant getUpdatedAt() { return updatedAt; }
    public CartItem setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
}
