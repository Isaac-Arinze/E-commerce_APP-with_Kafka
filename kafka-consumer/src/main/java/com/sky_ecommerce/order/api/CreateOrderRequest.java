package com.sky_ecommerce.order.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {
    @NotBlank
    private String customerId;

    @NotEmpty
    private List<LineItem> items;

    public static class LineItem {
        @NotBlank
        private String sku;
        private int quantity;
        private BigDecimal price;

        public String getSku() { return sku; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }

        public void setSku(String sku) { this.sku = sku; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public String getCustomerId() { return customerId; }
    public List<LineItem> getItems() { return items; }

    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setItems(List<LineItem> items) { this.items = items; }
}
