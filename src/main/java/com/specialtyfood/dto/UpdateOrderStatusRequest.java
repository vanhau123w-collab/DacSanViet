package com.specialtyfood.dto;

import com.specialtyfood.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating order status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;
    
    @Size(max = 100, message = "Mã vận đơn không được quá 100 ký tự")
    private String trackingNumber;
    
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String notes;
}