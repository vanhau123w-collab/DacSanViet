package com.dacsanviet.controller;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.dto.CreateOrderRequest;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Checkout controller - handles both page rendering and order processing
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Show checkout page - supports both authenticated users and guests
     * Cart is loaded from localStorage on client side
     */
    @GetMapping
    public String showCheckout(Model model, Authentication authentication) {
        try {
            CartDao cart = new CartDao();
            
            // Check if user is authenticated for display purposes
            if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userId", userPrincipal.getId());
            } else {
                model.addAttribute("isGuest", true);
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("orderRequest", new CreateOrderRequest());
            model.addAttribute("pageTitle", "Thanh Toán");
            
            return "checkout/simple-checkout";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * Checkout success page
     */
    @GetMapping("/success")
    public String checkoutSuccess(
            @RequestParam(required = false) String orderNumber,
            Model model) {
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("pageTitle", "Đặt Hàng Thành Công");
        return "checkout/success";
    }
    
    /**
     * Process checkout via AJAX JSON
     */
    @PostMapping(value = "/process", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processCheckoutJson(
            @RequestBody CreateOrderRequest orderRequest,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== CHECKOUT JSON PROCESS (CORRECT METHOD) ===");
            System.out.println("Request received: " + orderRequest);
            System.out.println("Customer Name: " + orderRequest.getCustomerName());
            System.out.println("Items count: " + (orderRequest.getItems() != null ? orderRequest.getItems().size() : 0));
            System.out.println("Content-Type: application/json - USING JSON METHOD");
            
            // Validate request
            if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
                response.put("success", false);
                response.put("error", "Giỏ hàng trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if user is authenticated
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getId();
                System.out.println("User authenticated: ID = " + userId);
            } else {
                System.out.println("Guest checkout");
            }
            
            // Set user ID (null for guest orders)
            orderRequest.setUserId(userId);
            
            // Create order - items come from request body (localStorage)
            OrderDao order = orderService.createOrderFromCart(orderRequest);
            System.out.println("Order created: " + order.getOrderNumber());
            
            // Build response
            response.put("success", true);
            response.put("orderNumber", order.getOrderNumber());
            response.put("orderId", order.getId());
            
            // Handle payment method redirect
            String paymentMethod = orderRequest.getPaymentMethod();
            if ("VNPAY".equals(paymentMethod)) {
                response.put("redirectUrl", "/payment/vnpay/create?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Thanh+toan+don+hang+" + order.getOrderNumber());
            } else if ("VIETQR".equals(paymentMethod)) {
                response.put("redirectUrl", "/payment/vietqr?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Don+hang+" + order.getOrderNumber());
            } else if ("MOMO".equals(paymentMethod)) {
                response.put("redirectUrl", "/payment/momo?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Don+hang+" + order.getOrderNumber());
            } else {
                // COD - redirect to success page
                response.put("redirectUrl", "/checkout/success?orderNumber=" + order.getOrderNumber());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Checkout JSON error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Process checkout via form submission (fallback)
     */
    @PostMapping(value = "/process", consumes = "application/x-www-form-urlencoded")
    public String processCheckoutForm(
            @Valid CreateOrderRequest orderRequest,
            Authentication authentication,
            Model model) {
        
        try {
            System.out.println("=== CHECKOUT FORM PROCESS (FALLBACK METHOD) ===");
            System.out.println("WARNING: Using form submission instead of JSON!");
            System.out.println("This should NOT happen for logged-in users!");
            
            // Check if user is authenticated
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getId();
            }
            
            orderRequest.setUserId(userId);
            
            // Create order
            OrderDao order = orderService.createOrderFromCart(orderRequest);
            
            // Redirect based on payment method
            String paymentMethod = orderRequest.getPaymentMethod();
            if ("VNPAY".equals(paymentMethod)) {
                return "redirect:/payment/vnpay/create?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Thanh+toan+don+hang+" + order.getOrderNumber();
            } else if ("VIETQR".equals(paymentMethod)) {
                return "redirect:/payment/vietqr?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Don+hang+" + order.getOrderNumber();
            } else if ("MOMO".equals(paymentMethod)) {
                return "redirect:/payment/momo?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=Don+hang+" + order.getOrderNumber();
            } else {
                return "redirect:/checkout/success?orderNumber=" + order.getOrderNumber();
            }
            
        } catch (Exception e) {
            System.err.println("Form checkout error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            model.addAttribute("orderRequest", new CreateOrderRequest());
            return "checkout/simple-checkout";
        }
    }
}