package com.specialtyfood.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Order controller for order management
 */
@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Đơn Hàng Của Tôi");
            
            if (authentication != null && authentication.isAuthenticated()) {
                // For now, show empty orders list
                model.addAttribute("orders", java.util.List.of());
            } else {
                return "redirect:/login";
            }
            
            return "orders/list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách đơn hàng: " + e.getMessage());
            return "orders/list";
        }
    }
}