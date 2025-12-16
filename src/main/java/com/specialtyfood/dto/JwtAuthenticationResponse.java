package com.specialtyfood.dto;

/**
 * JWT Authentication response DTO
 */
public class JwtAuthenticationResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserDto user;
    
    // Default constructor
    public JwtAuthenticationResponse() {}
    
    // Constructor
    public JwtAuthenticationResponse(String accessToken, String refreshToken, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
}