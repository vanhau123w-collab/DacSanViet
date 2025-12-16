package com.specialtyfood.controller;

import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class DataInitController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/init-data")
    @ResponseBody
    public String initializeData() {
        try {
            // Create categories if they don't exist
            if (categoryRepository.count() == 0) {
                createCategories();
            }
            
            // Create products if they don't exist
            if (productRepository.count() == 0) {
                createProducts();
            }
            
            // Create admin user if doesn't exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                createAdminUser();
            }
            
            return "Dữ liệu mẫu đã được tạo thành công!";
        } catch (Exception e) {
            return "Lỗi khi tạo dữ liệu mẫu: " + e.getMessage();
        }
    }
    
    @GetMapping("/clear-test-data")
    @ResponseBody
    public String clearTestData() {
        try {
            // Clear all test users except admin
            userRepository.deleteAll(userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("admin"))
                .toList());
            
            return "Đã xóa dữ liệu test thành công! Chỉ giữ lại tài khoản admin.";
        } catch (Exception e) {
            return "Lỗi khi xóa dữ liệu test: " + e.getMessage();
        }
    }
    
    @GetMapping("/check-conflicts")
    @ResponseBody
    public String checkConflicts() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("Kiểm tra conflicts:\n");
            
            // Check for duplicate usernames
            result.append("- Username 'admin': ").append(userRepository.existsByUsername("admin") ? "Tồn tại" : "Không tồn tại").append("\n");
            
            // Check for duplicate emails
            result.append("- Email 'admin@dacsanquenhuong.vn': ").append(userRepository.existsByEmail("admin@dacsanquenhuong.vn") ? "Tồn tại" : "Không tồn tại").append("\n");
            result.append("- Email 'vanhaul23w@gmail.com': ").append(userRepository.existsByEmail("vanhaul23w@gmail.com") ? "Tồn tại" : "Không tồn tại").append("\n");
            
            // Check for duplicate phone numbers
            result.append("- Phone '0869872247': ").append(userRepository.existsByPhoneNumber("0869872247") ? "Tồn tại" : "Không tồn tại").append("\n");
            
            // Count total users
            result.append("- Tổng số users: ").append(userRepository.count()).append("\n");
            
            return result.toString();
        } catch (Exception e) {
            return "Lỗi khi kiểm tra conflicts: " + e.getMessage();
        }
    }
    
    @GetMapping("/view-database")
    @ResponseBody
    public String viewDatabase() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DATABASE OVERVIEW ===\n\n");
            
            // Users
            result.append("📋 USERS (" + userRepository.count() + " records):\n");
            userRepository.findAll().forEach(user -> {
                result.append("- ID: ").append(user.getId())
                      .append(", Username: ").append(user.getUsername())
                      .append(", Email: ").append(user.getEmail())
                      .append(", Phone: ").append(user.getPhoneNumber())
                      .append(", Role: ").append(user.getRole())
                      .append(", Active: ").append(user.getIsActive())
                      .append("\n");
            });
            
            // Categories
            result.append("\n📂 CATEGORIES (" + categoryRepository.count() + " records):\n");
            categoryRepository.findAll().forEach(category -> {
                result.append("- ID: ").append(category.getId())
                      .append(", Name: ").append(category.getName())
                      .append(", Active: ").append(category.getIsActive())
                      .append("\n");
            });
            
            // Products
            result.append("\n🛍️ PRODUCTS (" + productRepository.count() + " records):\n");
            productRepository.findAll().forEach(product -> {
                result.append("- ID: ").append(product.getId())
                      .append(", Name: ").append(product.getName())
                      .append(", Price: ").append(product.getPrice())
                      .append("₫, Stock: ").append(product.getStockQuantity())
                      .append(", Category: ").append(product.getCategory().getName())
                      .append(", Featured: ").append(product.getIsFeatured())
                      .append("\n");
            });
            
            return result.toString();
        } catch (Exception e) {
            return "Lỗi khi xem database: " + e.getMessage();
        }
    }
    
    @GetMapping("/database")
    public String databaseViewer(org.springframework.ui.Model model) {
        try {
            // Get all data
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("pageTitle", "Database Viewer");
            
            return "database-viewer";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu database: " + e.getMessage());
            return "database-viewer";
        }
    }
    
    private void createCategories() {
        List<Category> categories = Arrays.asList(
            new Category("Đặc Sản Miền Bắc", "Các sản phẩm đặc sản từ miền Bắc Việt Nam"),
            new Category("Đặc Sản Miền Trung", "Các sản phẩm đặc sản từ miền Trung Việt Nam"),
            new Category("Đặc Sản Miền Nam", "Các sản phẩm đặc sản từ miền Nam Việt Nam"),
            new Category("Bánh Kẹo", "Các loại bánh kẹo truyền thống"),
            new Category("Gia Vị", "Gia vị và nước chấm đặc sản"),
            new Category("Trái Cây Sấy", "Trái cây sấy khô các loại")
        );
        
        categoryRepository.saveAll(categories);
    }
    
    private void createProducts() {
        List<Category> categories = categoryRepository.findAll();
        
        List<Product> products = Arrays.asList(
            // Miền Bắc
            createProduct("Bánh Chưng Truyền Thống", "Bánh chưng làm từ gạo nếp, đậu xanh và thịt heo, gói lá dong", 
                         new BigDecimal("150000"), 50, categories.get(0), true, "https://via.placeholder.com/300x200/4ade80/ffffff?text=Bánh+Chưng"),
            createProduct("Nem Chua Thanh Hóa", "Nem chua làm từ thịt heo tươi, gia vị đặc biệt", 
                         new BigDecimal("80000"), 30, categories.get(0), true, "https://via.placeholder.com/300x200/f59e0b/ffffff?text=Nem+Chua"),
            createProduct("Chả Cá Lã Vọng", "Chả cá truyền thống Hà Nội với hương vị đặc trưng", 
                         new BigDecimal("200000"), 25, categories.get(0), false, "https://via.placeholder.com/300x200/ef4444/ffffff?text=Chả+Cá"),
            
            // Miền Trung
            createProduct("Bún Bò Huế Khô", "Bún bò Huế khô đặc sản, gia vị chuẩn vị", 
                         new BigDecimal("45000"), 100, categories.get(1), true, "https://via.placeholder.com/300x200/8b5cf6/ffffff?text=Bún+Bò+Huế"),
            createProduct("Bánh Khoái Huế", "Bánh khoái truyền thống với tôm, thịt và rau sống", 
                         new BigDecimal("35000"), 40, categories.get(1), false, "https://via.placeholder.com/300x200/06b6d4/ffffff?text=Bánh+Khoái"),
            createProduct("Mắm Ruốc Huế", "Mắm ruốc đặc sản Huế, hương vị đậm đà", 
                         new BigDecimal("120000"), 60, categories.get(1), true, "https://via.placeholder.com/300x200/f97316/ffffff?text=Mắm+Ruốc"),
            
            // Miền Nam
            createProduct("Bánh Tráng Nướng", "Bánh tráng nướng Đà Lạt với đầy đủ topping", 
                         new BigDecimal("25000"), 80, categories.get(2), true, "https://via.placeholder.com/300x200/10b981/ffffff?text=Bánh+Tráng"),
            createProduct("Hủ Tiếu Khô", "Hủ tiếu khô Sài Gòn với tôm khô và thịt băm", 
                         new BigDecimal("40000"), 70, categories.get(2), false, "https://via.placeholder.com/300x200/ec4899/ffffff?text=Hủ+Tiếu"),
            createProduct("Bánh Xèo Miền Tây", "Bánh xèo giòn rụm với tôm, thịt và giá đỗ", 
                         new BigDecimal("30000"), 50, categories.get(2), true, "https://via.placeholder.com/300x200/84cc16/ffffff?text=Bánh+Xèo"),
            
            // Bánh kẹo
            createProduct("Kẹo Dừa Bến Tre", "Kẹo dừa thơm ngon từ dừa tươi Bến Tre", 
                         new BigDecimal("60000"), 90, categories.get(3), true, "https://via.placeholder.com/300x200/fbbf24/ffffff?text=Kẹo+Dừa"),
            createProduct("Bánh Đậu Xanh", "Bánh đậu xanh mềm mịn, thơm ngon", 
                         new BigDecimal("75000"), 45, categories.get(3), false, "https://via.placeholder.com/300x200/22c55e/ffffff?text=Bánh+Đậu"),
            createProduct("Kẹo Lạc Hà Nội", "Kẹo lạc giòn tan, vị ngọt thanh", 
                         new BigDecimal("50000"), 65, categories.get(3), true, "https://via.placeholder.com/300x200/a855f7/ffffff?text=Kẹo+Lạc")
        );
        
        productRepository.saveAll(products);
    }
    
    private Product createProduct(String name, String description, BigDecimal price, 
                                 int stock, Category category, boolean featured, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setIsFeatured(featured);
        product.setImageUrl(imageUrl);
        product.setIsActive(true);
        product.setWeightGrams(500); // Default weight
        product.setOrigin("Việt Nam");
        return product;
    }
    
    private void createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@dacsanquenhuong.vn");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Quản Trị Viên");
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);
        
        userRepository.save(admin);
    }
}