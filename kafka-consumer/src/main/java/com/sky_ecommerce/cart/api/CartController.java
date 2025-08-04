package com.sky_ecommerce.cart.api;

import com.sky_ecommerce.cart.domain.Cart;
import com.sky_ecommerce.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
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

    @GetMapping
    public Cart getCart(Principal principal,
                        @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        return carts.getCart(userId);
    }

    @PostMapping("/items")
    public Cart addItem(@RequestBody Map<String, Object> body,
                        Principal principal,
                        @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        String productId = (String) body.get("productId");
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        return carts.addItem(userId, productId, quantity);
    }

    @PutMapping("/items/{productId}")
    public Cart updateItem(@PathVariable String productId,
                           @RequestBody Map<String, Object> body,
                           Principal principal,
                           @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        return carts.updateItem(userId, productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public Cart removeItem(@PathVariable String productId,
                           Principal principal,
                           @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        return carts.removeItem(userId, productId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Principal principal,
                                      @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String userId = currentUserId(principal, headerUser);
        carts.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
