package com.sky_ecommerce.product.api;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Helper DTO for binding multipart form fields when creating a product with images in one request.
 */
public class ProductCreateMultipartRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String status; // DRAFT, ACTIVE, INACTIVE
    private MultipartFile[] images; // optional multiple images

    public String getTitle() { return title; }
    public ProductCreateMultipartRequest setTitle(String title) { this.title = title; return this; }

    public String getDescription() { return description; }
    public ProductCreateMultipartRequest setDescription(String description) { this.description = description; return this; }

    public BigDecimal getPrice() { return price; }
    public ProductCreateMultipartRequest setPrice(BigDecimal price) { this.price = price; return this; }

    public Integer getStockQty() { return stockQty; }
    public ProductCreateMultipartRequest setStockQty(Integer stockQty) { this.stockQty = stockQty; return this; }

    public String getStatus() { return status; }
    public ProductCreateMultipartRequest setStatus(String status) { this.status = status; return this; }

    public MultipartFile[] getImages() { return images; }
    public ProductCreateMultipartRequest setImages(MultipartFile[] images) { this.images = images; return this; }
}
