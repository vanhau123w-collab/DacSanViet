package com.specialtyfood.controller;

import com.specialtyfood.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Cart controller for shopping cart functionality
 */
@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Giỏ Hàng");
            
            if (authentication != null && authentication.isAuthenticated()) {
                // Get cart items for authenticated user
                // For now, we'll show empty cart
                model.addAttribute("cartItems", java.util.List.of());
                model.addAttribute("subtotal", 0);
            } else {
                // Redirect to login if not authenticated
                return "redirect:/login";
            }
            
            return "cart/index";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải giỏ hàng: " + e.getMessage());
            return "cart/index";
        }
    }
}