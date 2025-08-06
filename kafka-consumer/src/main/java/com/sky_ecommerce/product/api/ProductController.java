package com.sky_ecommerce.product.api;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sky_ecommerce.product.service.ProductService;
import com.sky_ecommerce.product.domain.Product;
import com.sky_ecommerce.product.category.domain.Category;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    private String currentUserId(Principal principal, @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        // Prefer authenticated principal; fallback to header for local testing
        if (principal != null && StringUtils.hasText(principal.getName())) {
            return principal.getName();
        }
        if (StringUtils.hasText(headerUser)) {
            return headerUser;
        }
        throw new IllegalStateException("Unauthenticated");
    }

    // Sellers: create product (JSON body)
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product request,
                                          Principal principal,
                                          @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String sellerId = currentUserId(principal, headerUser);
        Product created = products.create(sellerId, request);
        return ResponseEntity.ok(created);
    }

    // Sellers: create product with images (multipart form)
    // Accepts text fields + one or more files in a single request
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createMultipart(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") String priceStr,
            @RequestParam("stockQty") String stockQtyStr,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Principal principal,
            @RequestHeader(value = "X-User-Id", required = false) String headerUser
    ) throws IOException {
        String sellerId = currentUserId(principal, headerUser);

        // Parse numeric values
        BigDecimal price;
        Integer stockQty;
        try {
            price = new BigDecimal(priceStr);
            stockQty = Integer.parseInt(stockQtyStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid price or stockQty format"));
        }

        Product payload = new Product()
                .setTitle(title)
                .setDescription(description)
                .setPrice(price)
                .setStockQty(stockQty);

        if (StringUtils.hasText(status)) {
            try {
                payload.setStatus(Product.Status.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                payload.setStatus(Product.Status.ACTIVE);
            }
        }

        if (categoryId != null) {
            payload.setCategory(new Category().setId(categoryId));
        }
        Product created = products.create(sellerId, payload);

        // Save images if provided
        int sort = 0;
        if (images != null && images.length > 0) {
            Path uploadsDir = Path.of("kafka-consumer", "uploads", created.getId());
            Files.createDirectories(uploadsDir);

            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                String original = file.getOriginalFilename();
                String safeName = (original != null ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "image");
                String fileName = System.currentTimeMillis() + "_" + safeName;
                Path destination = uploadsDir.resolve(fileName);
                Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Optionally persist ProductImage rows (basic)
                com.sky_ecommerce.product.domain.ProductImage pi = new com.sky_ecommerce.product.domain.ProductImage()
                        .setProduct(created)
                        .setUrl("/uploads/" + created.getId() + "/" + fileName)
                        .setSortOrder(sort++);
                created.getImages().add(pi);
            }

            // Persist product with images list
            // Since Product.images is cascade=ALL and orphanRemoval=true, saving product will save images
            products.create(sellerId, created);
        }

        // Create a clean response without circular references
        Map<String, Object> productData = Map.of(
                "id", created.getId(),
                "sellerId", created.getSellerId(),
                "title", created.getTitle(),
                "description", created.getDescription() != null ? created.getDescription() : "",
                "price", created.getPrice(),
                "stockQty", created.getStockQty(),
                "status", created.getStatus().toString(),
                "categoryId", created.getCategory() != null ? created.getCategory().getId() : null,
                "imageCount", created.getImages().size()
        );
        
        return ResponseEntity.ok(Map.of(
                "product", productData,
                "uploadedCount", images == null ? 0 : images.length
        ));
    }

    // Sellers: update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id,
                                          @RequestBody Product request,
                                          Principal principal,
                                          @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String sellerId = currentUserId(principal, headerUser);
        Product updated = products.update(sellerId, id, request);
        return ResponseEntity.ok(updated);
    }

    // Sellers: delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       Principal principal,
                                       @RequestHeader(value = "X-User-Id", required = false) String headerUser) {
        String sellerId = currentUserId(principal, headerUser);
        products.delete(sellerId, id);
        return ResponseEntity.noContent().build();
    }

    // Public: get by id
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return products.getById(id).map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    // Public: search/list
    @GetMapping
    public Page<Product> search(@RequestParam(required = false) String q,
                                @RequestParam(required = false) String sellerId,
                                @RequestParam(required = false) BigDecimal minPrice,
                                @RequestParam(required = false) BigDecimal maxPrice,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return products.search(q, sellerId, minPrice, maxPrice, PageRequest.of(page, size));
    }

    // Seller: upload a product image (simple local filesystem storage)
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(@PathVariable String id,
                                                           @RequestParam("file") MultipartFile file,
                                                           Principal principal,
                                                           @RequestHeader(value = "X-User-Id", required = false) String headerUser) throws IOException {
        String sellerId = currentUserId(principal, headerUser);

        // Ensure product belongs to seller
        Product product = products.getById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: not your product"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "file is empty"));
        }

        // Create uploads directory if not exists
        Path uploadsDir = Path.of("kafka-consumer", "uploads", id);
        Files.createDirectories(uploadsDir);

        String original = file.getOriginalFilename();
        String safeName = (original != null ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "image");
        String fileName = System.currentTimeMillis() + "_" + safeName;
        Path destination = uploadsDir.resolve(fileName);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Public URL mapping (we'll serve /uploads/** via a resource handler later)
        String url = "/uploads/" + id + "/" + fileName;

        return ResponseEntity.ok(Map.of(
                "productId", id,
                "url", url
        ));
    }
}
