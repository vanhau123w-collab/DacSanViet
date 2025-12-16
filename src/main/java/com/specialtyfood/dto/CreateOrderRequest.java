package com.specialtyfood.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new order
 */
public class CreateOrderRequest {
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    @NotNull(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    // Default constructor
    public CreateOrderRequest() {}
    
    // Constructor with required fields
    public CreateOrderRequest(Long shippingAddressId, String paymentMethod) {
        this.shippingAddressId = shippingAddressId;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public Long getShippingAddressId() {
        return shippingAddressId;
    }
    
    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}