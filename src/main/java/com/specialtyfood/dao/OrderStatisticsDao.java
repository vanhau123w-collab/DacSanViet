package com.specialtyfood.dao;

import java.math.BigDecimal;

/**
 * DAO for order statistics
 */
public class OrderStatisticsDao {
    
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    
    // Default constructor
    public OrderStatisticsDao() {}
    
    // Getters and Setters
    public Long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public Long getPendingOrders() {
        return pendingOrders;
    }
    
    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
    
    public Long getConfirmedOrders() {
        return confirmedOrders;
    }
    
    public void setConfirmedOrders(Long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }
    
    public Long getShippedOrders() {
        return shippedOrders;
    }
    
    public void setShippedOrders(Long shippedOrders) {
        this.shippedOrders = shippedOrders;
    }
    
    public Long getDeliveredOrders() {
        return deliveredOrders;
    }
    
    public void setDeliveredOrders(Long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }
    
    public Long getCancelledOrders() {
        return cancelledOrders;
    }
    
    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}