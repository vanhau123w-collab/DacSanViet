package com.specialtyfood.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    // Default constructor
    public RefreshTokenRequest() {}
    
    // Constructor
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}