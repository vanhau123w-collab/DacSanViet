package com.specialtyfood.dto;

import com.specialtyfood.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating order status
 */
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    @Size(max = 100, message = "Tracking number must not exceed 100 characters")
    private String trackingNumber;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    // Default constructor
    public UpdateOrderStatusRequest() {}
    
    // Constructor with required fields
    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }
    
    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}