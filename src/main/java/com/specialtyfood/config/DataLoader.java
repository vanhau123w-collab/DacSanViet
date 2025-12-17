package com.specialtyfood.config;

import com.specialtyfood.model.Category;
import com.specialtyfood.model.Product;
import com.specialtyfood.model.User;
import com.specialtyfood.repository.CategoryRepository;
import com.specialtyfood.repository.ProductRepository;
import com.specialtyfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DataLoader to initialize default users
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@specialtyfood.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setAdmin(true);
            admin.setIsActive(true);
            
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: admin/admin123");
        }
        
        // Create default user if not exists
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@specialtyfood.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName("Test User");
            user.setAdmin(false);
            user.setIsActive(true);
            
            userRepository.save(user);
            System.out.println("✅ Default test user created: user/user123");
        }
        
        // Create sample categories and products
        createSampleData();
    }
    
    private void createSampleData() {
        // Create categories if they don't exist
        Category dacSanMienBac = createCategoryIfNotExists("Đặc Sản Miền Bắc", "Các sản phẩm đặc sản từ miền Bắc Việt Nam");
        Category dacSanMienTrung = createCategoryIfNotExists("Đặc Sản Miền Trung", "Các sản phẩm đặc sản từ miền Trung Việt Nam");
        Category dacSanMienNam = createCategoryIfNotExists("Đặc Sản Miền Nam", "Các sản phẩm đặc sản từ miền Nam Việt Nam");
        Category banhKeo = createCategoryIfNotExists("Bánh Kẹo", "Các loại bánh kẹo truyền thống");
        Category nemChua = createCategoryIfNotExists("Nem Chua", "Các loại nem chua đặc sản");
        
        // Create sample products
        createProductIfNotExists("Bánh Chưng Truyền Thống", "Bánh chưng làm từ gạo nếp, đậu xanh và thịt heo, gói lá dong", 
                new BigDecimal("150000"), 50, dacSanMienBac, "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=300&q=80", "Hà Nội", 500);
        
        createProductIfNotExists("Nem Chua Thanh Hóa", "Nem chua truyền thống Thanh Hóa với hương vị đặc trưng", 
                new BigDecimal("80000"), 100, nemChua, "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=300&q=80", "Thanh Hóa", 200);
        
        createProductIfNotExists("Bánh Tráng Nướng Đà Lạt", "Bánh tráng nướng giòn rụm với các loại topping đa dạng", 
                new BigDecimal("45000"), 200, dacSanMienTrung, "https://images.unsplash.com/photo-1559847844-d721426d6edc?w=300&q=80", "Đà Lạt", 100);
        
        createProductIfNotExists("Kẹo Dừa Bến Tre", "Kẹo dừa thơm ngon làm từ dừa tươi Bến Tre", 
                new BigDecimal("60000"), 150, banhKeo, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=300&q=80", "Bến Tre", 250);
        
        createProductIfNotExists("Chả Cá Lã Vọng", "Chả cá truyền thống Hà Nội với hương vị đặc biệt", 
                new BigDecimal("200000"), 30, dacSanMienBac, "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=300&q=80", "Hà Nội", 300);
        
        createProductIfNotExists("Bánh Ít Lá Gai", "Bánh ít lá gai truyền thống miền Trung với nhân tôm thịt", 
                new BigDecimal("35000"), 80, dacSanMienTrung, "https://images.unsplash.com/photo-1563379091339-03246963d96c?w=300&q=80", "Huế", 150);
        
        createProductIfNotExists("Mắm Ruốc Huế", "Mắm ruốc đặc sản Huế với hương vị đậm đà", 
                new BigDecimal("120000"), 60, dacSanMienTrung, "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=300&q=80", "Huế", 200);
        
        createProductIfNotExists("Bánh Căn Phan Thiết", "Bánh căn nướng giòn với nước chấm đặc biệt", 
                new BigDecimal("25000"), 120, dacSanMienNam, "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=300&q=80", "Phan Thiết", 80);
        
        System.out.println("✅ Sample categories and products created successfully!");
    }
    
    private Category createCategoryIfNotExists(String name, String description) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setIsActive(true);
            return categoryRepository.save(category);
        });
    }
    
    private Product createProductIfNotExists(String name, String description, BigDecimal price, 
                                           Integer stock, Category category, String imageUrl, 
                                           String origin, Integer weight) {
        return productRepository.findByName(name).orElseGet(() -> {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stock);
            product.setCategory(category);
            product.setImageUrl(imageUrl);
            product.setOrigin(origin);
            product.setWeightGrams(weight);
            product.setIsActive(true);
            product.setIsFeatured(Math.random() > 0.7); // 30% chance to be featured
            return productRepository.save(product);
        });
    }
}