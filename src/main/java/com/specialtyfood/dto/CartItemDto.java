package com.specialtyfood.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for CartItem
 */
public class CartItemDto {
    
    private Long id;
    
    @NotNull(message = "Product is required")
    private ProductDto product;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime addedDate;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public CartItemDto() {}
    
    // Constructor with required fields
    public CartItemDto(ProductDto product, Integer quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }
    
    // Constructor with all fields
    public CartItemDto(Long id, ProductDto product, Integer quantity, BigDecimal unitPrice,
                      LocalDateTime addedDate, LocalDateTime updatedAt) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
        this.addedDate = addedDate;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProductDto getProduct() {
        return product;
    }
    
    public void setProduct(ProductDto product) {
        this.product = product;
        this.totalPrice = calculateTotalPrice();
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public LocalDateTime getAddedDate() {
        return addedDate;
    }
    
    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    private BigDecimal calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    public boolean isAvailable() {
        return product != null && product.isAvailable() && 
               product.getStockQuantity() >= quantity;
    }
    
    public boolean hasInsufficientStock() {
        return product != null && product.getStockQuantity() < quantity;
    }
}