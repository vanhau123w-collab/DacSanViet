package com.specialtyfood.service;

import com.specialtyfood.dao.InventoryStatisticsDao;
import com.specialtyfood.dao.ProductDao;
import com.specialtyfood.dao.CategoryDao;
import com.specialtyfood.model.Product;
import com.specialtyfood.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for inventory management operations
 */
@Service
@Transactional
public class InventoryService {
    
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    
    // Low stock threshold - products with stock below this will trigger notifications
    private static final Integer LOW_STOCK_THRESHOLD = 10;
    
    @Autowired
    public InventoryService(ProductRepository productRepository,
                           NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Update product stock quantity
     */
    public ProductDao updateStock(Long productId, Integer newQuantity) {
        Product product = getProductById(productId);
        
        if (newQuantity < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }
        
        Integer oldQuantity = product.getStockQuantity();
        product.setStockQuantity(newQuantity);
        product = productRepository.save(product);
        
        // Check for low stock after update
        checkLowStockAndNotify(product);
        
        // Log stock change
        logStockChange(product, oldQuantity, newQuantity);
        
        return convertToProductDto(product);
    }
    
    /**
     * Increase stock quantity (e.g., when receiving new inventory)
     */
    public ProductDao increaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to increase must be positive");
        }
        
        Product product = getProductById(productId);
        Integer newQuantity = product.getStockQuantity() + quantity;
        product.setStockQuantity(newQuantity);
        product = productRepository.save(product);
        
        return convertToProductDto(product);
    }
    
    /**
     * Decrease stock quantity (e.g., when processing orders)
     * This method prevents overselling by checking availability first
     */
    public ProductDao decreaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to decrease must be positive");
        }
        
        Product product = getProductById(productId);
        
        // Prevent overselling
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + 
                                     ", Requested: " + quantity + " for product: " + product.getName());
        }
        
        Integer oldQuantity = product.getStockQuantity();
        Integer newQuantity = oldQuantity - quantity;
        product.setStockQuantity(newQuantity);
        product = productRepository.save(product);
        
        // Check for low stock after decrease
        checkLowStockAndNotify(product);
        
        // Log stock change
        logStockChange(product, oldQuantity, newQuantity);
        
        return convertToProductDto(product);
    }
    
    /**
     * Reserve stock for pending orders (soft reservation)
     */
    public boolean reserveStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        // Check if enough stock is available
        if (product.getStockQuantity() < quantity) {
            return false;
        }
        
        // For now, we'll implement a simple reservation by decreasing stock
        // In a more complex system, you might have a separate reserved_quantity field
        decreaseStock(productId, quantity);
        return true;
    }
    
    /**
     * Release reserved stock (e.g., when order is cancelled)
     */
    public ProductDao releaseReservedStock(Long productId, Integer quantity) {
        return increaseStock(productId, quantity);
    }
    
    /**
     * Check stock availability for a product
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, Integer requestedQuantity) {
        Product product = getProductById(productId);
        return product.getIsActive() && product.getStockQuantity() >= requestedQuantity;
    }
    
    /**
     * Get products with low stock
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getLowStockProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStockQuantityLessThanAndIsActiveTrue(LOW_STOCK_THRESHOLD, pageable);
        return products.map(this::convertToProductDto);
    }
    
    /**
     * Get out of stock products
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getOutOfStockProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStockQuantityAndIsActiveTrue(0, pageable);
        return products.map(this::convertToProductDto);
    }
    
    /**
     * Get all products with stock information
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getAllProductsWithStock(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::convertToProductDto);
    }
    
    /**
     * Bulk update stock quantities
     */
    public List<ProductDao> bulkUpdateStock(List<StockUpdateRequest> stockUpdates) {
        return stockUpdates.stream()
                .map(update -> updateStock(update.getProductId(), update.getNewQuantity()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check and notify for low stock across all products
     */
    public void checkAllProductsForLowStock() {
        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThanAndIsActiveTrue(LOW_STOCK_THRESHOLD);
        
        for (Product product : lowStockProducts) {
            notificationService.sendLowStockNotification(product);
        }
    }
    
    /**
     * Get inventory summary statistics
     */
    @Transactional(readOnly = true)
    public InventoryStatisticsDao getInventoryStatistics() {
        Long totalProducts = productRepository.countByIsActiveTrue();
        Long lowStockProducts = productRepository.countByStockQuantityLessThanAndIsActiveTrue(LOW_STOCK_THRESHOLD);
        Long outOfStockProducts = productRepository.countByStockQuantityAndIsActiveTrue(0);
        Long totalStockValue = productRepository.calculateTotalStockValue();
        
        InventoryStatisticsDao stats = new InventoryStatisticsDao();
        stats.setTotalProducts(totalProducts);
        stats.setLowStockProducts(lowStockProducts);
        stats.setOutOfStockProducts(outOfStockProducts);
        stats.setInStockProducts(totalProducts - outOfStockProducts);
        stats.setTotalStockValue(totalStockValue);
        
        return stats;
    }
    
    // Helper methods
    
    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }
    
    private void checkLowStockAndNotify(Product product) {
        if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD && product.getIsActive()) {
            notificationService.sendLowStockNotification(product);
        }
    }
    
    private void logStockChange(Product product, Integer oldQuantity, Integer newQuantity) {
        // Log stock changes for audit purposes
        System.out.println("Stock changed for product " + product.getName() + 
                          " (ID: " + product.getId() + "): " + 
                          oldQuantity + " -> " + newQuantity);
        
        // In a real application, you might want to store this in a stock_movements table
        // or use a proper logging framework
    }
    
    private ProductDao convertToProductDto(Product product) {
        // Convert Product entity to ProductDao
        // This is a simplified conversion - you might want to use a mapper library like MapStruct
        ProductDao dto = new ProductDao();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        dto.setIsFeatured(product.getIsFeatured());
        dto.setWeightGrams(product.getWeightGrams());
        dto.setOrigin(product.getOrigin());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Convert category if present
        if (product.getCategory() != null) {
            CategoryDao CategoryDao = new CategoryDao();
            CategoryDao.setId(product.getCategory().getId());
            CategoryDao.setName(product.getCategory().getName());
            CategoryDao.setDescription(product.getCategory().getDescription());
            dto.setCategory(CategoryDao);
        }
        
        return dto;
    }
    
    /**
     * Inner class for stock update requests
     */
    public static class StockUpdateRequest {
        private Long productId;
        private Integer newQuantity;
        
        public StockUpdateRequest() {}
        
        public StockUpdateRequest(Long productId, Integer newQuantity) {
            this.productId = productId;
            this.newQuantity = newQuantity;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Integer getNewQuantity() {
            return newQuantity;
        }
        
        public void setNewQuantity(Integer newQuantity) {
            this.newQuantity = newQuantity;
        }
    }
}