package com.specialtyfood.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Shopping Cart
 */
public class CartDao {
    
    private Long userId;
    private List<CartItemDao> items;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    
    // Default constructor
    public CartDao() {
        this.items = new ArrayList<>();
        this.totalItems = 0;
        this.totalQuantity = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
    }
    
    // Constructor with user ID
    public CartDao(Long userId) {
        this();
        this.userId = userId;
    }
    
    // Constructor with items
    public CartDao(Long userId, List<CartItemDao> items) {
        this.userId = userId;
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public List<CartItemDao> getItems() {
        return items;
    }
    
    public void setItems(List<CartItemDao> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
    public BigDecimal getShipping() {
        return shipping;
    }
    
    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }
    
    // Helper methods
    public void addItem(CartItemDao item) {
        if (item != null) {
            items.add(item);
            calculateTotals();
        }
    }
    
    public void removeItem(CartItemDao item) {
        if (item != null) {
            items.remove(item);
            calculateTotals();
        }
    }
    
    public void removeItemById(Long itemId) {
        items.removeIf(item -> item.getId() != null && item.getId().equals(itemId));
        calculateTotals();
    }
    
    public void removeItemByProductId(Long productId) {
        items.removeIf(item -> item.getProduct() != null && 
                              item.getProduct().getId() != null && 
                              item.getProduct().getId().equals(productId));
        calculateTotals();
    }
    
    public CartItemDao findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProduct() != null && 
                               item.getProduct().getId() != null && 
                               item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasItem(Long productId) {
        return findItemByProductId(productId) != null;
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public void clear() {
        items.clear();
        calculateTotals();
    }
    
    public List<CartItemDao> getUnavailableItems() {
        return items.stream()
                .filter(item -> !item.isAvailable())
                .toList();
    }
    
    public List<CartItemDao> getItemsWithInsufficientStock() {
        return items.stream()
                .filter(CartItemDao::hasInsufficientStock)
                .toList();
    }
    
    public boolean hasUnavailableItems() {
        return items.stream().anyMatch(item -> !item.isAvailable());
    }
    
    public boolean hasInsufficientStockItems() {
        return items.stream().anyMatch(CartItemDao::hasInsufficientStock);
    }
    
    private void calculateTotals() {
        if (items == null || items.isEmpty()) {
            this.totalItems = 0;
            this.totalQuantity = 0;
            this.subtotal = BigDecimal.ZERO;
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        
        this.totalItems = items.size();
        this.totalQuantity = items.stream()
                .mapToInt(CartItemDao::getQuantity)
                .sum();
        
        this.subtotal = items.stream()
                .map(CartItemDao::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // For now, tax and shipping are zero, but can be calculated based on business rules
        this.totalAmount = subtotal.add(tax).add(shipping);
    }
    
    public void recalculateTotals() {
        calculateTotals();
    }
}