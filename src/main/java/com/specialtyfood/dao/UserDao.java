package com.specialtyfood.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User DAO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDao {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean admin;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Analytics fields
    private Long orderCount;
    private BigDecimal totalSpent;
    
    // Constructor for basic user info
    public UserDao(Long id, String username, String email, String fullName, 
                   String phoneNumber, Boolean admin, Boolean isActive, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.admin = admin;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}