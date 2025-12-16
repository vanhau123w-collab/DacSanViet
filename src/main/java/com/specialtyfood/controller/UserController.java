package com.specialtyfood.controller;

import com.specialtyfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * User controller for user profile and account management
 */
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping({"/profile", "/user/profile"})
    public String profile(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Thông Tin Cá Nhân");
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                
                // Create sample user data for now
                model.addAttribute("user", createSampleUser(username));
            } else {
                return "redirect:/login";
            }
            
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin cá nhân: " + e.getMessage());
            return "user/profile";
        }
    }
    
    private Object createSampleUser(String username) {
        // This is a temporary implementation
        return new Object() {
            public String getUsername() { return username; }
            public String getFullName() { return "Người dùng " + username; }
            public String getEmail() { return username + "@example.com"; }
            public String getPhoneNumber() { return "0123456789"; }
            public String getRole() { return "USER"; }
            public boolean isActive() { return true; }
            public java.time.LocalDateTime getCreatedAt() { return java.time.LocalDateTime.now().minusDays(30); }
        };
    }
}

// Also add profile route to HomeController