package com.dacsanviet.config;

import com.dacsanviet.model.*;
import com.dacsanviet.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DataLoader to initialize default users and sample data
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private NewsCategoryRepository newsCategoryRepository;
    
    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@dacsanviet.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: admin/admin123");
        }
        
        // Create default user if not exists
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@dacsanviet.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName("Test User");
            user.setRole(Role.USER);
            user.setIsActive(true);
            
            userRepository.save(user);
            System.out.println("✅ Default test user created: user/user123");
        }
        
        // Create sample categories and products
        createSampleData();
        
        // Create sample news data
        createSampleNewsData();
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
    
    private void createSampleNewsData() {
        // Get admin user for author
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            System.out.println("❌ Admin user not found, skipping news data creation");
            return;
        }
        
        // Create news categories
        NewsCategory khuyenMai = createNewsCategoryIfNotExists("Khuyến Mãi", "khuyen-mai", "Tin tức về các chương trình khuyến mãi");
        NewsCategory suKien = createNewsCategoryIfNotExists("Sự Kiện", "su-kien", "Tin tức về các sự kiện và lễ hội");
        NewsCategory dacSan = createNewsCategoryIfNotExists("Đặc Sản", "dac-san", "Tin tức về các món đặc sản Việt Nam");
        NewsCategory tinTuc = createNewsCategoryIfNotExists("Tin Tức Chung", "tin-tuc-chung", "Tin tức tổng hợp");
        
        // Create sample news articles
        createNewsArticleIfNotExists(
            "Khám Phá Đặc Sản Tết Nguyên Đán 2025 - Hương Vị Truyền Thống Không Thể Thiếu",
            "kham-pha-dac-san-tet-nguyen-dan-2025",
            "Tết Nguyên Đán đang đến gần, cùng khám phá những món đặc sản truyền thống không thể thiếu trong mâm cơm ngày Tết. Từ bánh chưng miền Bắc đến bánh tét miền Nam, mỗi món ăn đều mang trong mình câu chuyện văn hóa độc đáo của dân tộc Việt Nam.",
            "<h2>Bánh Chưng - Linh Hồn Của Tết Miền Bắc</h2><p>Bánh chưng không chỉ là món ăn mà còn là biểu tượng văn hóa của người Việt. Được làm từ gạo nếp, đậu xanh và thịt heo, gói trong lá dong xanh, bánh chưng thể hiện sự sum vầy, đoàn tụ của gia đình trong dịp Tết.</p><h2>Bánh Tét - Nét Đẹp Miền Nam</h2><p>Khác với bánh chưng hình vuông, bánh tét miền Nam có hình tròn dài, được gói bằng lá chuối. Hương vị ngọt ngào của bánh tét pha lẫn mùi thơm của lá chuối tạo nên một trải nghiệm ẩm thực khó quên.</p>",
            "Tết Nguyên Đán đang đến gần, cùng khám phá những món đặc sản truyền thống không thể thiếu trong mâm cơm ngày Tết.",
            "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=800&q=80",
            "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&q=80",
            dacSan, admin, true, NewsStatus.PUBLISHED
        );
        
        createNewsArticleIfNotExists(
            "Bí Quyết Làm Nem Chua Thanh Hóa Chuẩn Vị",
            "bi-quyet-lam-nem-chua-thanh-hoa-chuan-vi",
            "Nem chua Thanh Hóa nổi tiếng với hương vị chua ngọt đặc trưng. Cùng tìm hiểu bí quyết làm nem chua thơm ngon như người địa phương.",
            "<h2>Nguyên Liệu Cần Chuẩn Bị</h2><p>Để làm nem chua Thanh Hóa ngon, bạn cần chuẩn bị: thịt heo ba chỉ tươi, tỏi, ớt, muối, đường, nước mắm và lá chuối non.</p><h2>Quy Trình Làm Nem</h2><p>Thịt heo được thái nhỏ, trộn đều với gia vị rồi ủ trong thời gian nhất định để tạo độ chua tự nhiên. Sau đó gói trong lá chuối và để trong môi trường ấm áp.</p>",
            "Nem chua Thanh Hóa nổi tiếng với hương vị chua ngọt đặc trưng. Cùng tìm hiểu bí quyết làm nem chua thơm ngon như người địa phương.",
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=800&q=80",
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400&q=80",
            dacSan, admin, false, NewsStatus.PUBLISHED
        );
        
        createNewsArticleIfNotExists(
            "Chương Trình Khuyến Mãi Tháng 12 - Giảm Giá Lên Đến 30%",
            "chuong-trinh-khuyen-mai-thang-12",
            "Nhân dịp cuối năm, chúng tôi triển khai chương trình khuyến mãi lớn với mức giảm giá hấp dẫn cho tất cả sản phẩm đặc sản.",
            "<h2>Ưu Đãi Đặc Biệt</h2><p>Từ ngày 1/12 đến 31/12, tất cả sản phẩm đặc sản được giảm giá từ 10% đến 30%. Đặc biệt, các sản phẩm Tết được ưu đãi lên đến 25%.</p><h2>Điều Kiện Áp Dụng</h2><p>Chương trình áp dụng cho tất cả khách hàng, không giới hạn số lượng mua. Miễn phí vận chuyển cho đơn hàng từ 500.000đ.</p>",
            "Nhân dịp cuối năm, chúng tôi triển khai chương trình khuyến mãi lớn với mức giảm giá hấp dẫn cho tất cả sản phẩm đặc sản.",
            "https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?w=800&q=80",
            "https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?w=400&q=80",
            khuyenMai, admin, true, NewsStatus.PUBLISHED
        );
        
        createNewsArticleIfNotExists(
            "Lễ Hội Ẩm Thực Huế 2025 - Tôn Vinh Văn Hóa Ẩm Thực Cung Đình",
            "le-hoi-am-thuc-hue-2025",
            "Lễ hội ẩm thực Huế 2025 sẽ diễn ra vào tháng 3, mang đến cơ hội thưởng thức những món ăn cung đình tinh tế và đặc sắc.",
            "<h2>Chương Trình Lễ Hội</h2><p>Lễ hội sẽ có các hoạt động trình diễn nấu ăn, triển lãm ẩm thực và không gian thưởng thức các món ăn truyền thống Huế.</p><h2>Đặc Sản Nổi Bật</h2><p>Du khách sẽ được thưởng thức bún bò Huế, bánh bèo, bánh nậm và nhiều món ăn cung đình khác.</p>",
            "Lễ hội ẩm thực Huế 2025 sẽ diễn ra vào tháng 3, mang đến cơ hội thưởng thức những món ăn cung đình tinh tế và đặc sắc.",
            "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80",
            "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80",
            suKien, admin, false, NewsStatus.PUBLISHED
        );
        
        createNewsArticleIfNotExists(
            "Khai Trương Cửa Hàng Đặc Sản Việt Chi Nhánh Mới",
            "khai-truong-cua-hang-dac-san-viet-chi-nhanh-moi",
            "Chúng tôi vui mừng thông báo khai trương chi nhánh mới tại quận 7, mang đến cho khách hàng nhiều lựa chọn đặc sản hơn.",
            "<h2>Địa Chỉ Mới</h2><p>Chi nhánh mới tại 123 Nguyễn Thị Thập, Quận 7, TP.HCM với không gian rộng rãi và đa dạng sản phẩm.</p><h2>Ưu Đãi Khai Trương</h2><p>Giảm 20% cho tất cả sản phẩm trong tuần đầu khai trương. Tặng quà cho 100 khách hàng đầu tiên.</p>",
            "Chúng tôi vui mừng thông báo khai trương chi nhánh mới tại quận 7, mang đến cho khách hàng nhiều lựa chọn đặc sản hơn.",
            "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800&q=80",
            "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80",
            tinTuc, admin, false, NewsStatus.PUBLISHED
        );
        
        System.out.println("✅ Sample news data created successfully!");
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
    
    private NewsCategory createNewsCategoryIfNotExists(String name, String slug, String description) {
        return newsCategoryRepository.findByName(name).orElseGet(() -> {
            NewsCategory category = new NewsCategory();
            category.setName(name);
            category.setSlug(slug);
            category.setDescription(description);
            category.setIsActive(true);
            category.setSortOrder(0);
            return newsCategoryRepository.save(category);
        });
    }
    
    private NewsArticle createNewsArticleIfNotExists(String title, String slug, String excerpt, String content,
                                                   String metaDescription, String featuredImage, String thumbnailImage,
                                                   NewsCategory category, User author, boolean isFeatured, NewsStatus status) {
        return newsArticleRepository.findBySlug(slug).orElseGet(() -> {
            NewsArticle article = new NewsArticle();
            article.setTitle(title);
            article.setSlug(slug);
            article.setExcerpt(excerpt);
            article.setContent(content);
            article.setMetaDescription(metaDescription);
            article.setFeaturedImage(featuredImage);
            article.setThumbnailImage(thumbnailImage);
            article.setCategory(category);
            article.setAuthor(author);
            article.setIsFeatured(isFeatured);
            article.setStatus(status);
            article.setViewCount(Math.round(Math.random() * 1000)); // Random view count for demo
            if (status == NewsStatus.PUBLISHED) {
                article.setPublishedAt(LocalDateTime.now().minusDays(Math.round(Math.random() * 30))); // Random publish date within last 30 days
            }
            return newsArticleRepository.save(article);
        });
    }
    
    private Product createProductIfNotExists(String name, String description, BigDecimal price, 
                                           Integer stock, Category category, String imageUrl, 
                                           String origin, Integer weight) {
        try {
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
        } catch (Exception e) {
            // If duplicate found, just return the first one
            logger.warn("Duplicate product found: {}, skipping creation", name);
            return productRepository.findAll().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        }
    }
}