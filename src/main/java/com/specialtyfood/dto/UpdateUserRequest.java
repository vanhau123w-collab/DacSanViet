package com.specialtyfood.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password; // Optional for updates
    
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;
    
    private Boolean admin = false;
    
    private Boolean isActive = true;
}