package com.specialtyfood.controller;

import com.specialtyfood.dao.CategoryDao;
import com.specialtyfood.dto.CreateCategoryRequest;
import com.specialtyfood.dto.UpdateCategoryRequest;
import com.specialtyfood.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Category controller for category management operations
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Get all active categories (public endpoint)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveCategories() {
        try {
            List<CategoryDao> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get all active categories error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get active categories");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get all categories with pagination (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCategories(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "name") String sortBy,
                                            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CategoryDao> categories = categoryService.getAllCategories(pageable);
            
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get all categories error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get categories");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDao category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
            
        } catch (RuntimeException e) {
            logger.error("Get category by ID error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Get category by ID error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Search categories by keyword (admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchCategories(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "name") String sortBy,
                                            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CategoryDao> categories = categoryService.searchCategories(keyword, pageable);
            
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Search categories error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search categories");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get categories with active products (public endpoint)
     */
    @GetMapping("/with-products")
    public ResponseEntity<?> getCategoriesWithProducts() {
        try {
            List<CategoryDao> categories = categoryService.getCategoriesWithProducts();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get categories with products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get categories with products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get categories with product count (admin only)
     */
    @GetMapping("/with-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCategoriesWithProductCount() {
        try {
            List<CategoryDao> categories = categoryService.getCategoriesWithProductCount();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get categories with product count error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get categories with product count");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get categories ordered by product count (public endpoint)
     */
    @GetMapping("/by-popularity")
    public ResponseEntity<?> getCategoriesOrderedByProductCount() {
        try {
            List<CategoryDao> categories = categoryService.getCategoriesOrderedByProductCount();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get categories by popularity error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get categories by popularity");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get empty categories (admin only)
     */
    @GetMapping("/empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmptyCategories() {
        try {
            List<CategoryDao> categories = categoryService.getEmptyCategories();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            logger.error("Get empty categories error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get empty categories");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Create a new category (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        try {
            CategoryDao category = categoryService.createCategory(request);
            
            logger.info("Category created: {}", category.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
            
        } catch (RuntimeException e) {
            logger.error("Create category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Create category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Update an existing category (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, 
                                          @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            CategoryDao category = categoryService.updateCategory(id, request);
            
            logger.info("Category updated: {}", category.getName());
            return ResponseEntity.ok(category);
            
        } catch (RuntimeException e) {
            logger.error("Update category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Update category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Delete a category (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            
            logger.info("Category deleted with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Delete category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Delete category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Toggle category active status (admin only)
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Long id) {
        try {
            CategoryDao category = categoryService.toggleCategoryStatus(id);
            
            logger.info("Category status toggled for ID: {}", id);
            return ResponseEntity.ok(category);
            
        } catch (RuntimeException e) {
            logger.error("Toggle category status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Toggle category status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to toggle category status");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Count products in category
     */
    @GetMapping("/{id}/product-count")
    public ResponseEntity<?> countProductsInCategory(@PathVariable Long id) {
        try {
            Long count = categoryService.countProductsInCategory(id);
            
            Map<String, Long> response = new HashMap<>();
            response.put("productCount", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Count products in category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to count products in category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}