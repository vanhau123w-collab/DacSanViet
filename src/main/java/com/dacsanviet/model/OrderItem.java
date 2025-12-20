package com.dacsanviet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem entity representing individual items within an order
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice; // Price at the time of order
    
    @Column(name = "product_name", nullable = false, length = 200)
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName; // Snapshot of product name at time of order
    
    @Column(name = "product_description", length = 2000)
    @Size(max = 2000, message = "Product description must not exceed 2000 characters")
    private String productDescription; // Snapshot of product description
    
    @Column(name = "category_name", length = 100)
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName; // Snapshot of category name
    
    @Column(name = "product_image_url", length = 500)
    @Size(max = 500, message = "Product image URL must not exceed 500 characters")
    private String productImageUrl; // Snapshot of product image
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructor with required fields
    public OrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        
        // Create snapshots of product information
        if (product != null) {
            this.productName = product.getName();
            this.productDescription = product.getDescription();
            this.productImageUrl = product.getImageUrl();
            if (product.getCategory() != null) {
                this.categoryName = product.getCategory().getName();
            }
        }
    }
    
    // Constructor from CartItem
    public OrderItem(Order order, CartItem cartItem) {
        this(order, cartItem.getProduct(), cartItem.getQuantity(), cartItem.getUnitPrice());
    }
    
    // Custom setter for product to update snapshots
    public void setProduct(Product product) {
        this.product = product;
        
        // Update snapshots when product changes
        if (product != null) {
            this.productName = product.getName();
            this.productDescription = product.getDescription();
            this.productImageUrl = product.getImageUrl();
            if (product.getCategory() != null) {
                this.categoryName = product.getCategory().getName();
            }
        }
    }
    
    // Helper methods
    public BigDecimal getTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getDiscountAmount() {
        // Calculate discount if current product price is different from unit price
        if (product != null && product.getPrice() != null && unitPrice != null) {
            BigDecimal currentPrice = product.getPrice();
            if (currentPrice.compareTo(unitPrice) < 0) {
                return unitPrice.subtract(currentPrice).multiply(BigDecimal.valueOf(quantity));
            }
        }
        return BigDecimal.ZERO;
    }
    
    public boolean hasDiscount() {
        return getDiscountAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}