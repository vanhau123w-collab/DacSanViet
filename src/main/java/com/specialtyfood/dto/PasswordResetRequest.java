package com.specialtyfood.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for password reset request
 */
public class PasswordResetRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    // Default constructor
    public PasswordResetRequest() {}
    
    // Constructor
    public PasswordResetRequest(String email) {
        this.email = email;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}