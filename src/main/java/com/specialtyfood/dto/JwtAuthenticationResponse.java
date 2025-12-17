package com.specialtyfood.dto;

import com.specialtyfood.dao.UserDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for JWT authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserDao user;
    
    // Constructor
    public JwtAuthenticationResponse(String accessToken, String refreshToken, UserDao user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}