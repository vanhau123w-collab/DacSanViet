package com.specialtyfood.dao;

/**
 * DAO for inventory statistics
 */
public class InventoryStatisticsDao {
    
    private Long totalProducts;
    private Long inStockProducts;
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private Long totalStockValue;
    
    // Default constructor
    public InventoryStatisticsDao() {}
    
    // Getters and Setters
    public Long getTotalProducts() {
        return totalProducts;
    }
    
    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    public Long getInStockProducts() {
        return inStockProducts;
    }
    
    public void setInStockProducts(Long inStockProducts) {
        this.inStockProducts = inStockProducts;
    }
    
    public Long getLowStockProducts() {
        return lowStockProducts;
    }
    
    public void setLowStockProducts(Long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }
    
    public Long getOutOfStockProducts() {
        return outOfStockProducts;
    }
    
    public void setOutOfStockProducts(Long outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }
    
    public Long getTotalStockValue() {
        return totalStockValue;
    }
    
    public void setTotalStockValue(Long totalStockValue) {
        this.totalStockValue = totalStockValue;
    }
}