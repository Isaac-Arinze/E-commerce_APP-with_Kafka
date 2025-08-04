package com.sky_ecommerce.product.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_seller", columnList = "sellerId"),
        @Index(name = "idx_products_status", columnList = "status"),
        @Index(name = "idx_products_created", columnList = "createdAt")
})
public class Product {

    public enum Status { DRAFT, ACTIVE, INACTIVE }

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stockQty = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public Product setId(String id) { this.id = id; return this; }

    public String getSellerId() { return sellerId; }
    public Product setSellerId(String sellerId) { this.sellerId = sellerId; return this; }

    public String getTitle() { return title; }
    public Product setTitle(String title) { this.title = title; return this; }

    public String getDescription() { return description; }
    public Product setDescription(String description) { this.description = description; return this; }

    public BigDecimal getPrice() { return price; }
    public Product setPrice(BigDecimal price) { this.price = price; return this; }

    public Integer getStockQty() { return stockQty; }
    public Product setStockQty(Integer stockQty) { this.stockQty = stockQty; return this; }

    public Status getStatus() { return status; }
    public Product setStatus(Status status) { this.status = status; return this; }

    public List<ProductImage> getImages() { return images; }
    public Product setImages(List<ProductImage> images) { this.images = images; return this; }

    public Instant getCreatedAt() { return createdAt; }
    public Product setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }

    public Instant getUpdatedAt() { return updatedAt; }
    public Product setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
}
