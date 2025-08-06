package com.sky_ecommerce.product.api;

import java.math.BigDecimal;

public class ProductDtos {

    public static class CreateProductRequest {
        public String sellerId;
        public String title;
        public String description;
        public BigDecimal price;
        public Integer stockQty;
        public Long categoryId; // optional: link product to a category
    }

    public static class UpdateProductRequest {
        public String title;
        public String description;
        public BigDecimal price;
        public Integer stockQty;
        public String status; // DRAFT, ACTIVE, INACTIVE
        public Long categoryId; // optional: change product category
    }
}
