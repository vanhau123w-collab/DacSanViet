package com.dacsanviet.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DAO for OrderItem entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDao {
    
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productDescription;
    private String categoryName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
    
    // Constructor with required fields
    public OrderItemDao(Long id, Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Helper methods
    public BigDecimal getTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}