package com.specialtyfood.controller;

import com.specialtyfood.dto.AddToCartRequest;
import com.specialtyfood.dao.CartDao;
import com.specialtyfood.dto.UpdateCartItemRequest;
import com.specialtyfood.security.UserPrincipal;
import com.specialtyfood.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

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
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                CartDao cart = cartService.getCart(userPrincipal.getId());
                
                model.addAttribute("cart", cart);
                model.addAttribute("cartItems", cart.getItems());
                model.addAttribute("subtotal", cart.getTotalAmount());
                model.addAttribute("itemCount", cart.getTotalItems());
            } else {
                return "redirect:/login";
            }
            
            return "cart/simple-index";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải giỏ hàng: " + e.getMessage());
            return "cart/simple-index";
        }
    }
    
    /**
     * Add product to cart (AJAX)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request, 
                                      Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            CartDao cart = cartService.addToCart(userPrincipal.getId(), request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm sản phẩm vào giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Update cart item quantity (AJAX)
     */
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request,
                                           Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            CartDao cart = cartService.updateCartItem(userPrincipal.getId(), request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Remove item from cart (AJAX)
     */
    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId,
                                           Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            CartDao cart = cartService.removeFromCart(userPrincipal.getId(), productId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Clear entire cart (AJAX)
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            cartService.clearCart(userPrincipal.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng",
                "cartItemCount", 0,
                "cartTotal", 0
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}