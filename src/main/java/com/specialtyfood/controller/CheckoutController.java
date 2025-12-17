package com.specialtyfood.controller;

import com.specialtyfood.dao.CartDao;
import com.specialtyfood.dto.CreateOrderRequest;
import com.specialtyfood.dao.OrderDao;
import com.specialtyfood.security.UserPrincipal;
import com.specialtyfood.service.CartService;
import com.specialtyfood.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

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
     * Show checkout page
     */
    @GetMapping
    public String showCheckout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            CartDao cart = cartService.getCart(userPrincipal.getId());
            
            if (cart.getItems().isEmpty()) {
                model.addAttribute("error", "Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
                return "redirect:/cart";
            }
            
            // Validate cart before checkout
            if (!cartService.validateCart(userPrincipal.getId())) {
                cartService.removeUnavailableItems(userPrincipal.getId());
                model.addAttribute("error", "Một số sản phẩm trong giỏ hàng không còn khả dụng và đã được xóa.");
                return "redirect:/cart";
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
     * Process checkout
     */
    @PostMapping("/process")
    public String processCheckout(
            @Valid @ModelAttribute("orderRequest") CreateOrderRequest orderRequest,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            if (bindingResult.hasErrors()) {
                CartDao cart = cartService.getCart(userPrincipal.getId());
                model.addAttribute("cart", cart);
                model.addAttribute("pageTitle", "Thanh Toán");
                return "checkout/simple-checkout";
            }
            
            // Set user ID for the order
            orderRequest.setUserId(userPrincipal.getId());
            
            // Create order
            OrderDao order = orderService.createOrderFromCart(orderRequest);
            
            // Clear cart after successful order
            cartService.clearCart(userPrincipal.getId());
            
            // Set appropriate success message based on payment method
            String successMessage;
            if ("COD".equals(orderRequest.getPaymentMethod())) {
                successMessage = "Đặt hàng COD thành công! Mã đơn hàng: " + order.getOrderNumber() + 
                               ". Đơn hàng đang được xử lý và sẽ được giao đến bạn sớm nhất.";
            } else {
                successMessage = "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber();
            }
            
            redirectAttributes.addFlashAttribute("message", successMessage);
            
            return "redirect:/orders/" + order.getId();
            
        } catch (Exception e) {
            CartDao cart = cartService.getCart(((UserPrincipal) authentication.getPrincipal()).getId());
            model.addAttribute("cart", cart);
            model.addAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            model.addAttribute("pageTitle", "Thanh Toán");
            return "checkout/simple-checkout";
        }
    }
    
    /**
     * Checkout success page
     */
    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam(required = false) Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("pageTitle", "Đặt Hàng Thành Công");
        return "checkout/success";
    }
}