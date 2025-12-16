package com.specialtyfood.dto;

import com.specialtyfood.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Order entity
 */
public class OrderDto {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private OrderStatus status;
    private List<OrderItemDto> orderItems = new ArrayList<>();
    private AddressDto shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String trackingNumber;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public OrderDto() {}
    
    // Constructor with required fields
    public OrderDto(Long id, String orderNumber, Long userId, BigDecimal totalAmount, OrderStatus status) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getShippingFee() {
        return shippingFee;
    }
    
    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public List<OrderItemDto> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemDto> orderItems) {
        this.orderItems = orderItems;
    }
    
    public AddressDto getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(AddressDto shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDateTime getShippedDate() {
        return shippedDate;
    }
    
    public void setShippedDate(LocalDateTime shippedDate) {
        this.shippedDate = shippedDate;
    }
    
    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }
    
    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    // Helper methods
    public void addOrderItem(OrderItemDto orderItem) {
        this.orderItems.add(orderItem);
    }
    
    public BigDecimal calculateSubtotal() {
        return orderItems.stream()
                .map(OrderItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal calculateGrandTotal() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal shipping = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        return subtotal.add(shipping).add(tax);
    }
    
    public Integer getTotalItems() {
        return orderItems.stream()
                .mapToInt(OrderItemDto::getQuantity)
                .sum();
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
}