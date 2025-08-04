package com.sky_ecommerce.product.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_images", indexes = {
        @Index(name = "idx_product_images_product", columnList = "product_id"),
        @Index(name = "idx_product_images_sort", columnList = "sortOrder")
})
public class ProductImage {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public ProductImage setId(String id) { this.id = id; return this; }

    public Product getProduct() { return product; }
    public ProductImage setProduct(Product product) { this.product = product; return this; }

    public String getUrl() { return url; }
    public ProductImage setUrl(String url) { this.url = url; return this; }

    public Integer getSortOrder() { return sortOrder; }
    public ProductImage setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }

    public Instant getCreatedAt() { return createdAt; }
    public ProductImage setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }
}
