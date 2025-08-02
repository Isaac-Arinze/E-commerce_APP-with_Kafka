package com.sky_ecommerce.order.service.events;

import java.math.BigDecimal;

// SOLID: Keep event payloads in a dedicated package independent of controllers/services.
// These are simple POJOs used as Kafka payloads for Order domain events.
public final class OrderEvents {

    private OrderEvents() {
        // utility holder
    }

    public static class OrderCreated {
        private String orderId;
        private String customerId;
        private BigDecimal total;

        public OrderCreated() {}

        public OrderCreated(String orderId, String customerId, BigDecimal total) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.total = total;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }

    public static class OrderPaid {
        private String orderId;

        public OrderPaid() {}

        public OrderPaid(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }

    public static class OrderCancelled {
        private String orderId;
        private String reason;

        public OrderCancelled() {}

        public OrderCancelled(String orderId, String reason) {
            this.orderId = orderId;
            this.reason = reason;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getReason() {
            return reason;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
