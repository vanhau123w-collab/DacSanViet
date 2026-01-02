package com.dacsanviet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating a new order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    private Long userId;
    
    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng không được quá 100 ký tự")
    @JsonProperty("customerName")
    private String customerName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    @NotBlank(message = "Email không được để trống")
    @JsonProperty("customerEmail")
    private String customerEmail;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ giao hàng không được quá 500 ký tự")
    @JsonProperty("shippingAddress")
    private String shippingAddress;
    
    @Size(max = 50, message = "Phương thức thanh toán không được quá 50 ký tự")
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    @JsonProperty("notes")
    private String notes;
    
    // For guest checkout - cart totals from localStorage
    @JsonProperty("subtotal")
    private BigDecimal subtotal;
    
    @JsonProperty("shippingFee")
    private BigDecimal shippingFee;
    
    // For guest checkout - cart items from localStorage
    // Initialize with empty ArrayList to allow Spring to bind form data
    @JsonProperty("items")
    private List<CartItemRequest> items = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemRequest {
        @JsonProperty("productId")
        private Long productId;
        
        @JsonProperty("productName")
        private String productName;
        
        @JsonProperty("productImageUrl")
        private String productImageUrl;
        
        @JsonProperty("quantity")
        private Integer quantity;
        
        @JsonProperty("unitPrice")
        private BigDecimal unitPrice;
    }
}
