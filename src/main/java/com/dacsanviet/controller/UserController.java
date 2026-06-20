package com.dacsanviet.controller;

import com.dacsanviet.model.Address;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.AddressRepository;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.dacsanviet.dto.ChangePasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * User controller for user profile and account management
 */
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping({"/profile", "/user/profile"})
    public String profile(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Thông Tin Cá Nhân");
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                Long userId = userPrincipal.getId();
                
                // Load user from database
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                // Load user addresses
                List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                
                model.addAttribute("user", user);
                model.addAttribute("addresses", addresses);
                model.addAttribute("addressCount", addresses.size());
                
                // Check if user has default address
                boolean hasDefaultAddress = addressRepository.existsByUserIdAndIsDefaultTrue(userId);
                model.addAttribute("hasDefaultAddress", hasDefaultAddress);
                
            } else {
                return "redirect:/login";
            }
            
            return "user/simple-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin cá nhân: " + e.getMessage());
            return "user/simple-profile";
        }
    }

    @PostMapping("/api/user/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Bạn cần đăng nhập để thực hiện chức năng này.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if old password matches
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Mật khẩu hiện tại không chính xác.");
                return ResponseEntity.badRequest().body(response);
            }

            // Update new password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}