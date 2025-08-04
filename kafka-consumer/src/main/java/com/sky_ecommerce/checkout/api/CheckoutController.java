package com.sky_ecommerce.checkout.api;

import com.sky_ecommerce.cart.domain.Cart;
import com.sky_ecommerce.cart.service.CartService;
import com.sky_ecommerce.order.domain.Order;
import com.sky_ecommerce.order.domain.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    public CheckoutController(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    private String currentUserId(Principal principal, @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        if (principal != null && StringUtils.hasText(principal.getName())) {
            return principal.getName();
        }
        if (StringUtils.hasText(headerUser)) {
            return headerUser;
        }
        throw new IllegalStateException("Unauthenticated");
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(Principal principal,
                                                        @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        // Compute total
        BigDecimal total = cart.getItems().stream()
                .map(ci -> ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build an Order entity from existing domain (minimal fields)
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomerId(userId);
        order.setStatus(Order.Status.PENDING);
        order.setTotal(total);
        // Persist order
        orderRepository.save(order);

        // Create a mock payment reference; client will call webhook to confirm
        String paymentReference = "pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "amount", total,
                "currency", "NGN",
                "paymentReference", paymentReference,
                "paymentProvider", "mock"
        ));
    }

    // Mock webhook to mark payment success
    @PostMapping("/payments/webhook")
    public ResponseEntity<Map<String, Object>> paymentWebhook(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId");
        String status = (String) payload.get("status"); // expected "success"
        if (!StringUtils.hasText(orderId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "orderId required"));
        }

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if ("success".equalsIgnoreCase(status)) {
            order.setStatus(Order.Status.PAID);
            orderRepository.save(order);
        }

        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name()
        ));
    }
}
