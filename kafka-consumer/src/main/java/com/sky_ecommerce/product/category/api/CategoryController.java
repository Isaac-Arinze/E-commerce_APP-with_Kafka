package com.sky_ecommerce.product.category.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sky_ecommerce.product.category.api.CategoryDtos.CategoryResponse;
import com.sky_ecommerce.product.category.api.CategoryDtos.CreateCategoryRequest;
import com.sky_ecommerce.product.category.api.CategoryDtos.UpdateCategoryRequest;
import com.sky_ecommerce.product.category.domain.Category;
import com.sky_ecommerce.product.category.service.CategoryService;

@RestController
@RequestMapping(path = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    private final CategoryService categories;

    public CategoryController(CategoryService categories) {
        this.categories = categories;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public CategoryResponse create(@RequestBody CreateCategoryRequest req) {
        Category c = categories.create(req.name, req.description, req.parentId);
        return CategoryResponse.from(c);
    }

    @GetMapping
    public Page<CategoryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categories.list(pageable).map(CategoryResponse::from);
    }

    @GetMapping(params = "parentId")
    public Page<CategoryResponse> listByParent(
            @RequestParam Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categories.listByParent(parentId, pageable).map(CategoryResponse::from);
    }

    @GetMapping("/{slug}")
    public CategoryResponse getBySlug(@PathVariable String slug) {
        return CategoryResponse.from(categories.getBySlug(slug));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CategoryResponse update(@PathVariable Long id, @RequestBody UpdateCategoryRequest req) {
        Category c = categories.update(id, req.name, req.description, req.parentId);
        return CategoryResponse.from(c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categories.delete(id);
    }
}
