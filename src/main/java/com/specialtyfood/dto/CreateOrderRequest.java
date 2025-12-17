package com.specialtyfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String customerName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String customerPhone;
    
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String customerEmail;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ giao hàng không được quá 500 ký tự")
    private String shippingAddress;
    
    @Size(max = 50, message = "Phương thức thanh toán không được quá 50 ký tự")
    private String paymentMethod;
    
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String notes;
}