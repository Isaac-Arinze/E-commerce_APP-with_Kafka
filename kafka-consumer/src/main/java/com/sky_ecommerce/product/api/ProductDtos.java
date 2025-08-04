package com.sky_ecommerce.product.api;

import java.math.BigDecimal;

public class ProductDtos {
    public static class CreateOrUpdateProductRequest {
        public String title;
        public String description;
        public BigDecimal price;
        public Integer stockQty;
        public String status; // DRAFT, ACTIVE, INACTIVE
    }
}
