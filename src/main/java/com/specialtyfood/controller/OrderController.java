package com.specialtyfood.controller;

import com.specialtyfood.dao.OrderDao;
import com.specialtyfood.security.UserPrincipal;
import com.specialtyfood.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Order controller for order management
 */
@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Đơn Hàng Của Tôi");
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                // Load actual orders from database
                var orders = orderService.getOrdersByUser(userPrincipal.getId(), 
                    org.springframework.data.domain.PageRequest.of(0, 20));
                model.addAttribute("orders", orders.getContent());
            } else {
                return "redirect:/login";
            }
            
            return "orders/simple-list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách đơn hàng: " + e.getMessage());
            return "orders/simple-list";
        }
    }
    
    /**
     * Show order details
     */
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            OrderDao order = orderService.getOrderById(id);
            
            // Verify user owns this order
            if (!order.getUserId().equals(userPrincipal.getId())) {
                model.addAttribute("error", "Bạn không có quyền xem đơn hàng này.");
                return "error/403";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng #" + order.getOrderNumber());
            
            return "orders/order-details";
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin đơn hàng: " + e.getMessage());
            return "error/404";
        }
    }
}