package com.specialtyfood.controller;

import com.specialtyfood.dto.CategoryDto;
import com.specialtyfood.dto.ProductDto;
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
            Page<ProductDto> featuredProducts = productService.getFeaturedProducts(pageable);
            model.addAttribute("featuredProducts", featuredProducts.getContent());
            
            // Get active categories
            List<CategoryDto> categories = categoryService.getAllActiveCategories();
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
    
    @GetMapping("/products")
    public String products(Model model, 
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "12") int size) {
        try {
            model.addAttribute("pageTitle", "Sản Phẩm");
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDto> products;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Search products by keyword
                Long categoryId = null;
                if (category != null && !category.trim().isEmpty()) {
                    try {
                        categoryId = Long.parseLong(category);
                    } catch (NumberFormatException e) {
                        // Invalid category ID, ignore
                    }
                }
                products = productService.searchProducts(keyword.trim(), categoryId, pageable);
                model.addAttribute("searchKeyword", keyword);
            } else if (category != null && !category.trim().isEmpty()) {
                // Filter by category
                try {
                    Long categoryId = Long.parseLong(category);
                    products = productService.getProductsByCategory(categoryId, pageable);
                    model.addAttribute("selectedCategory", category);
                } catch (NumberFormatException e) {
                    // Invalid category ID, show all products
                    products = productService.getAllProducts(pageable);
                }
            } else {
                // Show all products
                products = productService.getAllProducts(pageable);
            }
            
            model.addAttribute("products", products.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("totalElements", products.getTotalElements());
            
            // Get categories for filter
            List<CategoryDto> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("error", "Không thể tải danh sách sản phẩm");
        }
        
        return "products/index";
    }
    
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
                Page<ProductDto> products = productService.searchProducts(keyword.trim(), null, pageable);
                
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
            List<CategoryDto> categories = categoryService.getCategoriesWithProductCount();
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