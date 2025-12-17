package com.specialtyfood.controller;

import com.specialtyfood.dao.CategoryDao;
import com.specialtyfood.dao.ProductDao;
import com.specialtyfood.service.CategoryService;
import com.specialtyfood.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Home controller for basic navigation and public pages
 */
@Controller
public class HomeController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Get featured products for homepage
            Pageable pageable = PageRequest.of(0, 4);
            Page<ProductDao> featuredProducts = productService.getFeaturedProducts(pageable);
            model.addAttribute("featuredProducts", featuredProducts.getContent());
            
            // Get active categories
            List<CategoryDao> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            
        } catch (Exception e) {
            // If there's an error, just continue without data
            model.addAttribute("featuredProducts", List.of());
            model.addAttribute("categories", List.of());
        }
        
        return "index";
    }
    
    @GetMapping("/health")
    public String health() {
        return "index";
    }
    
    // Products mapping moved to ProductController
    
    @GetMapping("/products/search")
    public String searchProducts(Model model, 
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size) {
        try {
            model.addAttribute("pageTitle", "Tìm Kiếm Sản Phẩm");
            model.addAttribute("searchKeyword", keyword);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ProductDao> products = productService.searchProducts(keyword.trim(), null, pageable);
                
                model.addAttribute("products", products.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", products.getTotalPages());
                model.addAttribute("totalElements", products.getTotalElements());
            } else {
                model.addAttribute("products", List.of());
            }
            
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("error", "Không thể tìm kiếm sản phẩm");
        }
        
        return "products/search";
    }
    
    @GetMapping("/categories")
    public String categories(Model model) {
        try {
            model.addAttribute("pageTitle", "Danh Mục Sản Phẩm");
            
            // Get categories with product count
            List<CategoryDao> categories = categoryService.getCategoriesWithProductCount();
            model.addAttribute("categories", categories);
            
        } catch (Exception e) {
            model.addAttribute("categories", List.of());
            model.addAttribute("error", "Không thể tải danh sách danh mục");
        }
        
        return "categories/index";
    }
    
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Đăng Nhập");
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Đăng Ký");
        return "auth/register";
    }
    
    @GetMapping("/test-simple")
    public String testSimple() {
        return "test-simple";
    }
    

}