package com.sky_ecommerce.cart.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_user", columnList = "userId", unique = true)
})
public class Cart {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true)
    private String userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public Cart setId(String id) { this.id = id; return this; }

    public String getUserId() { return userId; }
    public Cart setUserId(String userId) { this.userId = userId; return this; }

    public List<CartItem> getItems() { return items; }
    public Cart setItems(List<CartItem> items) { this.items = items; return this; }

    public Instant getCreatedAt() { return createdAt; }
    public Cart setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }

    public Instant getUpdatedAt() { return updatedAt; }
    public Cart setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
}
