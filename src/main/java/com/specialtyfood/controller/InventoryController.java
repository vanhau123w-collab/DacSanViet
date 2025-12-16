package com.specialtyfood.controller;

import com.specialtyfood.dto.InventoryStatisticsDto;
import com.specialtyfood.dto.ProductDto;
import com.specialtyfood.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for inventory management operations
 */
@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * Update product stock quantity
     */
    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<ProductDto> updateStock(@PathVariable Long productId,
                                                 @RequestParam Integer quantity) {
        try {
            ProductDto product = inventoryService.updateStock(productId, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Increase product stock quantity
     */
    @PutMapping("/products/{productId}/stock/increase")
    public ResponseEntity<ProductDto> increaseStock(@PathVariable Long productId,
                                                   @RequestParam Integer quantity) {
        try {
            ProductDto product = inventoryService.increaseStock(productId, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Decrease product stock quantity
     */
    @PutMapping("/products/{productId}/stock/decrease")
    public ResponseEntity<ProductDto> decreaseStock(@PathVariable Long productId,
                                                   @RequestParam Integer quantity) {
        try {
            ProductDto product = inventoryService.decreaseStock(productId, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check stock availability
     */
    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<Boolean> checkStockAvailability(@PathVariable Long productId,
                                                         @RequestParam Integer quantity) {
        try {
            boolean available = inventoryService.isStockAvailable(productId, quantity);
            return ResponseEntity.ok(available);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get products with low stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<Page<ProductDto>> getLowStockProducts(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "stockQuantity") String sortBy,
                                                               @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDto> products = inventoryService.getLowStockProducts(pageable);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get out of stock products
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<Page<ProductDto>> getOutOfStockProducts(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "name") String sortBy,
                                                                 @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDto> products = inventoryService.getOutOfStockProducts(pageable);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all products with stock information
     */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> getAllProductsWithStock(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "name") String sortBy,
                                                                   @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDto> products = inventoryService.getAllProductsWithStock(pageable);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Bulk update stock quantities
     */
    @PutMapping("/bulk-update")
    public ResponseEntity<List<ProductDto>> bulkUpdateStock(@RequestBody List<InventoryService.StockUpdateRequest> stockUpdates) {
        try {
            List<ProductDto> updatedProducts = inventoryService.bulkUpdateStock(stockUpdates);
            return ResponseEntity.ok(updatedProducts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check all products for low stock and send notifications
     */
    @PostMapping("/check-low-stock")
    public ResponseEntity<String> checkAllProductsForLowStock() {
        try {
            inventoryService.checkAllProductsForLowStock();
            return ResponseEntity.ok("Low stock check completed and notifications sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error checking low stock: " + e.getMessage());
        }
    }
    
    /**
     * Get inventory statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<InventoryStatisticsDto> getInventoryStatistics() {
        try {
            InventoryStatisticsDto statistics = inventoryService.getInventoryStatistics();
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reserve stock for a product
     */
    @PostMapping("/products/{productId}/reserve")
    public ResponseEntity<Boolean> reserveStock(@PathVariable Long productId,
                                               @RequestParam Integer quantity) {
        try {
            boolean reserved = inventoryService.reserveStock(productId, quantity);
            return ResponseEntity.ok(reserved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Release reserved stock for a product
     */
    @PostMapping("/products/{productId}/release")
    public ResponseEntity<ProductDto> releaseReservedStock(@PathVariable Long productId,
                                                          @RequestParam Integer quantity) {
        try {
            ProductDto product = inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}