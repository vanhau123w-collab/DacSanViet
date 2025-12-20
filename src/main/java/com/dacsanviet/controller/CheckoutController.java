package com.dacsanviet.controller;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dto.CreateOrderRequest;
import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.CartService;
import com.dacsanviet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Checkout controller for order processing
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Show checkout page - supports both authenticated users and guests
     * Cart is loaded from localStorage on client side (Yame behavior)
     */
    @GetMapping
    public String showCheckout(Model model, Authentication authentication) {
        try {
            // Always use guest checkout flow since cart is in localStorage
            // Cart will be loaded from localStorage on client side
            CartDao cart = new CartDao();
            
            // Check if user is authenticated for display purposes
            if (authentication != null && authentication.isAuthenticated()) {
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
     * Process checkout - supports both authenticated users and guests
     */
    @PostMapping("/process")
    public String processCheckout(
            @Valid @ModelAttribute("orderRequest") CreateOrderRequest orderRequest,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        
        try {
            // Validate form
            if (bindingResult.hasErrors()) {
                model.addAttribute("orderRequest", orderRequest);
                model.addAttribute("pageTitle", "Thanh Toán");
                return "checkout/simple-checkout";
            }
            
            // Check if user is authenticated
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getId();
            }
            
            // Set user ID (null for guest orders)
            orderRequest.setUserId(userId);
            
            // Create order
            OrderDao order = orderService.createOrderFromCart(orderRequest);
            
            // Clear cart after successful order (for authenticated users)
            if (userId != null) {
                cartService.clearCart(userId);
            }
            
            // Handle payment method
            String paymentMethod = orderRequest.getPaymentMethod();
            
            if ("VNPAY".equals(paymentMethod)) {
                // Redirect to VNPAY payment
                return "redirect:/payment/vnpay/create?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=" + java.net.URLEncoder.encode("Thanh toan don hang " + order.getOrderNumber(), "UTF-8");
            } else if ("VIETQR".equals(paymentMethod)) {
                // Redirect to VietQR payment page
                return "redirect:/payment/vietqr?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=" + java.net.URLEncoder.encode("Don hang " + order.getOrderNumber(), "UTF-8");
            } else if ("MOMO".equals(paymentMethod)) {
                // Redirect to Momo payment page
                return "redirect:/payment/momo?orderId=" + order.getId() + 
                       "&amount=" + order.getTotalAmount() + 
                       "&orderInfo=" + java.net.URLEncoder.encode("Don hang " + order.getOrderNumber(), "UTF-8");
            } else if ("COD".equals(paymentMethod)) {
                // COD - direct success
                String successMessage = "Đặt hàng COD thành công! Mã đơn hàng: " + order.getOrderNumber() + 
                                      ". Đơn hàng đang được xử lý và sẽ được giao đến bạn sớm nhất.";
                redirectAttributes.addFlashAttribute("message", successMessage);
                
                // For guest orders, redirect to a guest-friendly success page
                if (userId == null) {
                    redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
                    return "redirect:/checkout/success?orderNumber=" + order.getOrderNumber();
                }
                
                return "redirect:/orders/" + order.getId();
            }
            
            // Default fallback
            return "redirect:/checkout/success?orderNumber=" + order.getOrderNumber();
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            model.addAttribute("pageTitle", "Thanh Toán");
            return "checkout/simple-checkout";
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
}