package com.sky_ecommerce.cart.service;

import com.sky_ecommerce.cart.domain.Cart;
import com.sky_ecommerce.cart.domain.CartItem;
import com.sky_ecommerce.cart.domain.CartRepository;
import com.sky_ecommerce.product.domain.Product;
import com.sky_ecommerce.product.domain.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    @Transactional
    public Cart getOrCreateCart(String userId) {
        return carts.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return carts.save(c);
        });
    }

    @Transactional
    public Cart addItem(String userId, String productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;

        Cart cart = getOrCreateCart(userId);
        Product product = products.findById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // ensure stock
        if (product.getStockQty() != null && product.getStockQty() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        // find existing item
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst().orElse(null);

        if (item == null) {
            item = new CartItem()
                    .setCart(cart)
                    .setProductId(productId)
                    .setUnitPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                    .setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        return carts.save(cart);
    }

    @Transactional
    public Cart updateItem(String userId, String productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return removeItem(userId, productId);
        }

        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Item not found in cart"));

        // ensure stock
        Product product = products.findById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (product.getStockQty() != null && product.getStockQty() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        item.setQuantity(quantity);
        return carts.save(cart);
    }

    @Transactional
    public Cart removeItem(String userId, String productId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(ci -> ci.getProductId().equals(productId));
        return carts.save(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        carts.findByUserId(userId).ifPresent(c -> {
            c.getItems().clear();
            carts.save(c);
        });
    }

    @Transactional(readOnly = true)
    public Cart getCart(String userId) {
        return getOrCreateCart(userId);
    }
}
