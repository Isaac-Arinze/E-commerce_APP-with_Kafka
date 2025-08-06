package com.sky_ecommerce.product.category.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sky_ecommerce.product.category.domain.Category;
import com.sky_ecommerce.product.category.domain.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional
    public Category create(String name, String description, Long parentId) {
        if (categories.existsByName(name)) {
            throw new IllegalArgumentException("Category with name already exists: " + name);
        }
        Category c = new Category()
                .setName(name)
                .setSlug(slugify(name))
                .setDescription(description);
        if (parentId != null) {
            Category parent = categories.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + parentId));
            c.setParent(parent);
        }
        return categories.save(c);
    }

    @Transactional(readOnly = true)
    public Page<Category> list(Pageable pageable) {
        return categories.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Category> listByParent(Long parentId, Pageable pageable) {
        return categories.findAllByParentId(parentId, pageable);
    }

    @Transactional(readOnly = true)
    public Category getBySlug(String slug) {
        return categories.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Category not found for slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category update(Long id, String name, String description, Long parentId) {
        Category existing = getById(id);

        if (name != null && !name.equals(existing.getName())) {
            if (categories.existsByName(name)) {
                throw new IllegalArgumentException("Category with name already exists: " + name);
            }
            existing.setName(name);
            existing.setSlug(slugify(name));
        }
        if (description != null) {
            existing.setDescription(description);
        }
        if (parentId != null) {
            if (id.equals(parentId)) {
                throw new IllegalArgumentException("Category cannot be parent of itself");
            }
            Category parent = categories.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + parentId));
            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }
        return categories.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Category existing = getById(id);
        // Basic guard: if it has children, prevent deletion
        if (!existing.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with children");
        }
        categories.deleteById(id);
    }

    private String slugify(String input) {
        String slug = input == null ? "" : input.trim().toLowerCase();
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-{2,}", "-");
        if (slug.endsWith("-")) slug = slug.substring(0, slug.length() - 1);
        if (slug.startsWith("-")) slug = slug.substring(1);
        return slug;
    }
}
