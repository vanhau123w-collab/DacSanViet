package com.specialtyfood.dto;

import com.specialtyfood.model.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Product
 */
public class ProductDto {
    
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isActive;
    private Boolean isFeatured;
    
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weightGrams;
    
    @Size(max = 100, message = "Origin must not exceed 100 characters")
    private String origin;
    
    private CategoryDto category;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Analytics fields
    private Long totalSold;
    private BigDecimal totalRevenue;
    
    // Default constructor
    public ProductDto() {}
    
    // Constructor with all fields
    public ProductDto(Long id, String name, String description, BigDecimal price, 
                     Integer stockQuantity, String imageUrl, Boolean isActive, 
                     Boolean isFeatured, Integer weightGrams, String origin, 
                     CategoryDto category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isFeatured = isFeatured;
        this.weightGrams = weightGrams;
        this.origin = origin;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Constructor without timestamps (for creation)
    public ProductDto(String name, String description, BigDecimal price, 
                     Integer stockQuantity, String imageUrl, Boolean isActive, 
                     Boolean isFeatured, Integer weightGrams, String origin, 
                     CategoryDto category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isFeatured = isFeatured;
        this.weightGrams = weightGrams;
        this.origin = origin;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public Integer getWeightGrams() {
        return weightGrams;
    }
    
    public void setWeightGrams(Integer weightGrams) {
        this.weightGrams = weightGrams;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public CategoryDto getCategory() {
        return category;
    }
    
    public void setCategory(CategoryDto category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Long getTotalSold() {
        return totalSold;
    }
    
    public void setTotalSold(Long totalSold) {
        this.totalSold = totalSold;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    // Helper methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    public boolean isAvailable() {
        return isActive != null && isActive && isInStock();
    }
}