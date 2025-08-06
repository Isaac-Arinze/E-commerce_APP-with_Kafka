package com.sky_ecommerce.product.category.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    Optional<Category> findBySlug(String slug);
    Page<Category> findAllByParentId(Long parentId, Pageable pageable);
}
