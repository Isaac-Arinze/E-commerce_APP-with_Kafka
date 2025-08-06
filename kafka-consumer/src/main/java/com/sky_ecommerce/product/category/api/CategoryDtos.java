package com.sky_ecommerce.product.category.api;

import java.time.Instant;

import com.sky_ecommerce.product.category.domain.Category;

public final class CategoryDtos {

    private CategoryDtos() {}

    public static class CreateCategoryRequest {
        public String name;
        public String description;
        public Long parentId;
    }

    public static class UpdateCategoryRequest {
        public String name;
        public String description;
        public Long parentId; // null clears parent
    }

    public static class CategoryResponse {
        public Long id;
        public String name;
        public String slug;
        public String description;
        public Long parentId;
        public Instant createdAt;
        public Instant updatedAt;

        public static CategoryResponse from(Category c) {
            CategoryResponse dto = new CategoryResponse();
            dto.id = c.getId();
            dto.name = c.getName();
            dto.slug = c.getSlug();
            dto.description = c.getDescription();
            dto.parentId = c.getParent() != null ? c.getParent().getId() : null;
            dto.createdAt = c.getCreatedAt();
            dto.updatedAt = c.getUpdatedAt();
            return dto;
        }
    }
}
