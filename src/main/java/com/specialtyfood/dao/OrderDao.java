package com.specialtyfood.dao;

import com.specialtyfood.model.OrderStatus;
import com.specialtyfood.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Order entity
 */
@Data
public class OrderDao {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private OrderStatus status;
    private List<OrderItemDao> orderItems = new ArrayList<>();
    private AddressDao shippingAddress;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String trackingNumber;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime deliveryConfirmedAt;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddressText;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public OrderDao() {}
    
    // Constructor with required fields
    public OrderDao(Long id, String orderNumber, Long userId, BigDecimal totalAmount, OrderStatus status) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // Helper methods
    public void addOrderItem(OrderItemDao orderItem) {
        this.orderItems.add(orderItem);
    }
    
    public BigDecimal calculateSubtotal() {
        return orderItems.stream()
                .map(OrderItemDao::getTotalPrice)
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
                .mapToInt(OrderItemDao::getQuantity)
                .sum();
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
}