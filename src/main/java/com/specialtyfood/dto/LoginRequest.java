package com.specialtyfood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    private String usernameOrEmail;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}