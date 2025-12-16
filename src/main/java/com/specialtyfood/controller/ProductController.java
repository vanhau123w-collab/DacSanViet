package com.specialtyfood.controller;

import com.specialtyfood.dto.*;
import com.specialtyfood.service.ProductService;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product controller for product management operations
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductService productService;
    
    /**
     * Get all products with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "12") int size,
                                          @RequestParam(defaultValue = "name") String sortBy,
                                          @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductDto> products = productService.getAllProducts(pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get all products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
            
        } catch (RuntimeException e) {
            logger.error("Get product by ID error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Get product by ID error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get product");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Search products by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "12") int size,
                                          @RequestParam(defaultValue = "name") String sortBy,
                                          @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductDto> products = productService.searchProducts(keyword, categoryId, pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Search products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long categoryId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "12") int size,
                                                 @RequestParam(defaultValue = "name") String sortBy,
                                                 @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductDto> products = productService.getProductsByCategory(categoryId, pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get products by category error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get products by category");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get products by price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<?> getProductsByPriceRange(@RequestParam BigDecimal minPrice,
                                                   @RequestParam BigDecimal maxPrice,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "12") int size,
                                                   @RequestParam(defaultValue = "price") String sortBy,
                                                   @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductDto> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get products by price range error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get products by price range");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get featured products
     */
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "8") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDto> products = productService.getFeaturedProducts(pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get featured products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get featured products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get recent products
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentProducts(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "8") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDto> products = productService.getRecentProducts(pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get recent products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get recent products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get similar products (for recommendations)
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilarProducts(@PathVariable Long id,
                                              @RequestParam(defaultValue = "4") int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ProductDto> products = productService.getSimilarProducts(id, pageable);
            
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get similar products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get similar products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Create a new product (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request) {
        try {
            ProductDto product = productService.createProduct(request);
            
            logger.info("Product created: {}", product.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
            
        } catch (RuntimeException e) {
            logger.error("Create product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Create product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create product");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Update an existing product (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, 
                                         @Valid @RequestBody UpdateProductRequest request) {
        try {
            ProductDto product = productService.updateProduct(id, request);
            
            logger.info("Product updated: {}", product.getName());
            return ResponseEntity.ok(product);
            
        } catch (RuntimeException e) {
            logger.error("Update product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Update product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update product");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Delete a product (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product deleted successfully");
            
            logger.info("Product deleted with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Delete product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Delete product error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete product");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Toggle product active status (admin only)
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleProductStatus(@PathVariable Long id) {
        try {
            ProductDto product = productService.toggleProductStatus(id);
            
            logger.info("Product status toggled for ID: {}", id);
            return ResponseEntity.ok(product);
            
        } catch (RuntimeException e) {
            logger.error("Toggle product status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Toggle product status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to toggle product status");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Toggle product featured status (admin only)
     */
    @PutMapping("/{id}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleFeaturedStatus(@PathVariable Long id) {
        try {
            ProductDto product = productService.toggleFeaturedStatus(id);
            
            logger.info("Product featured status toggled for ID: {}", id);
            return ResponseEntity.ok(product);
            
        } catch (RuntimeException e) {
            logger.error("Toggle featured status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Toggle featured status error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to toggle featured status");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Update product stock (admin only)
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@PathVariable Long id, 
                                       @RequestBody Map<String, Integer> stockData) {
        try {
            Integer newStock = stockData.get("stock");
            if (newStock == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Stock quantity is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            ProductDto product = productService.updateStock(id, newStock);
            
            logger.info("Product stock updated for ID: {}", id);
            return ResponseEntity.ok(product);
            
        } catch (RuntimeException e) {
            logger.error("Update stock error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Update stock error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update stock");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Upload product image (admin only)
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = productService.uploadProductImage(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            
            logger.info("Product image uploaded: {}", imageUrl);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Upload image error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Upload image error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get low stock products (admin only)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        try {
            List<ProductDto> products = productService.getLowStockProducts(threshold);
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get low stock products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get low stock products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get out of stock products (admin only)
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOutOfStockProducts() {
        try {
            List<ProductDto> products = productService.getOutOfStockProducts();
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Get out of stock products error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get out of stock products");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}