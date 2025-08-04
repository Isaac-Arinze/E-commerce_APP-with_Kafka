package com.sky_ecommerce.product.service;

import com.sky_ecommerce.product.domain.Product;
import com.sky_ecommerce.product.domain.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public Product create(String sellerId, Product payload) {
        payload.setId(java.util.UUID.randomUUID().toString());
        payload.setSellerId(sellerId);
        if (payload.getStatus() == null) {
            payload.setStatus(Product.Status.ACTIVE);
        }
        return products.save(payload);
    }

    public Product update(String sellerId, String id, Product payload) {
        Product existing = products.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!existing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("Forbidden: not your product");
        }
        if (StringUtils.hasText(payload.getTitle())) existing.setTitle(payload.getTitle());
        if (payload.getDescription() != null) existing.setDescription(payload.getDescription());
        if (payload.getPrice() != null) existing.setPrice(payload.getPrice());
        if (payload.getStockQty() != null) existing.setStockQty(payload.getStockQty());
        if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        return products.save(existing);
    }

    public void delete(String sellerId, String id) {
        Product existing = products.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!existing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("Forbidden: not your product");
        }
        products.delete(existing);
    }

    public Optional<Product> getById(String id) {
        return products.findById(id);
    }

    public Page<Product> search(String q, String sellerId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + q.toLowerCase() + "%")
            ));
        }
        if (StringUtils.hasText(sellerId)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("sellerId"), sellerId));
        }
        if (minPrice != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), Product.Status.ACTIVE));

        return products.findAll(spec, pageable);
    }
}
