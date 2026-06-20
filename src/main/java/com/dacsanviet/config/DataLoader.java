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
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Autowired
    private SupplierRepository supplierRepository;

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

        // Backfill the richer catalog exported from the DacSanVietRail database.
        createRailwayCatalogData();
        
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

    private void createRailwayCatalogData() {
        Map<String, Category> categories = new LinkedHashMap<>();

        categories.put("Đặc Sản Miền Bắc", createOrUpdateCategory("Đặc Sản Miền Bắc",
                "Các sản phẩm đặc sản từ miền Bắc Việt Nam",
                "/uploads/categories/8ec9f056-1d64-450b-9eef-e3587f79b868.jpg", null));
        categories.put("Đặc Sản Miền Trung", createOrUpdateCategory("Đặc Sản Miền Trung",
                "Các sản phẩm đặc sản từ miền Trung Việt Nam",
                "/uploads/categories/9de89f4a-e524-4659-b682-f443777226df.webp", null));
        categories.put("Đặc Sản Miền Nam", createOrUpdateCategory("Đặc Sản Miền Nam",
                "Các sản phẩm đặc sản từ miền Nam Việt Nam",
                "/uploads/categories/b37e33bc-a9bf-4ef4-817b-1fddf2cdfaa1.jpg", null));

        categories.put("Bánh Kẹo", createOrUpdateCategory("Bánh Kẹo", "Các loại bánh kẹo truyền thống",
                "/uploads/categories/bfb54091-31cb-493f-b0d6-e2fb0efdb803.jpg", categories.get("Đặc Sản Miền Bắc")));
        categories.put("Nem Chua", createOrUpdateCategory("Nem Chua", "Các loại nem chua đặc sản",
                "/uploads/categories/27853534-c2e1-43e3-b781-6b06cc42af19.jpg", categories.get("Đặc Sản Miền Trung")));
        categories.put("Các Loại Mắm", createOrUpdateCategory("Các Loại Mắm",
                "Các loại mắm đặc sản miền Tây vô cùng thơm ngon",
                "/uploads/categories/47f547fd-a3a3-4dee-b507-0070cc00efe2.jpg", categories.get("Đặc Sản Miền Nam")));
        categories.put("Bến Tre", createOrUpdateCategory("Bến Tre", "Tỉnh Miền Tây",
                "/uploads/categories/34b3f4c0-25be-4be7-840d-a893924aee8d.jpg", categories.get("Đặc Sản Miền Nam")));
        categories.put("Kẹo dừa", createOrUpdateCategory("Kẹo dừa", "",
                "/uploads/categories/06743131-dbb4-40bf-a970-652f7b2ed941.jpg", categories.get("Bến Tre")));

        addProvinceCategories(categories, "Đặc Sản Miền Bắc", new String[][] {
                {"Hà Nội", "Đặc sản từ thủ đô Hà Nội - Bánh chưng, bánh giầy, chả cá Lã Vọng"},
                {"Hải Phòng", "Đặc sản từ thành phố cảng Hải Phòng - Bánh đa cua, nem cua bể"},
                {"Quảng Ninh", "Đặc sản từ Quảng Ninh - Ngọc trai, hải sản tươi sống"},
                {"Lạng Sơn", "Đặc sản từ Lạng Sơn - Bánh cuốn Lạng Sơn, măng khô"},
                {"Cao Bằng", "Đặc sản từ Cao Bằng - Bánh khảo, thịt trâu gác bếp"},
                {"Bắc Kạn", "Đặc sản từ Bắc Kạn - Cá suối, măng rừng"},
                {"Thái Nguyên", "Đặc sản từ Thái Nguyên - Chè Thái Nguyên, cốm xanh"},
                {"Lào Cai", "Đặc sản từ Lào Cai - Thịt trâu gác bếp, rượu cần"},
                {"Yên Bái", "Đặc sản từ Yên Bái - Hồng không hạt, bánh chưng gù"},
                {"Sơn La", "Đặc sản từ Sơn La - Mận Sơn La, cá suối nướng"},
                {"Điện Biên", "Đặc sản từ Điện Biên - Thịt trâu gác bếp, rượu cần"},
                {"Lai Châu", "Đặc sản từ Lai Châu - Chè Shan Tuyết, mật ong rừng"},
                {"Hà Giang", "Đặc sản từ Hà Giang - Thịt trâu gác bếp, mật ong đá"},
                {"Tuyên Quang", "Đặc sản từ Tuyên Quang - Bánh coóng phù, chè Tân Cương"},
                {"Phú Thọ", "Đặc sản từ Phú Thọ - Bánh chưng Phú Thọ, chè Phú Thọ"},
                {"Vĩnh Phúc", "Đặc sản từ Vĩnh Phúc - Bánh cuốn Thanh Trì, chả cá"},
                {"Bắc Giang", "Đặc sản từ Bắc Giang - Vải thiều Lục Ngạn, bánh đậu xanh"},
                {"Bắc Ninh", "Đặc sản từ Bắc Ninh - Bánh phu thê, chả cá Bắc Ninh"},
                {"Hải Dương", "Đặc sản từ Hải Dương - Bánh đậu xanh, nem Thanh Hà"},
                {"Hưng Yên", "Đặc sản từ Hưng Yên - Longan Hưng Yên, bánh đậu xanh"},
                {"Thái Bình", "Đặc sản từ Thái Bình - Bánh ít lá gai, nem chua"},
                {"Hà Nam", "Đặc sản từ Hà Nam - Bánh cuốn Phủ Lý, chả cá"},
                {"Nam Định", "Đặc sản từ Nam Định - Bánh gai, nem Yên Mạc"},
                {"Ninh Bình", "Đặc sản từ Ninh Bình - Cơm cháy, thịt dê núi"}
        });

        addProvinceCategories(categories, "Đặc Sản Miền Trung", new String[][] {
                {"Thanh Hóa", "Đặc sản từ Thanh Hóa - Nem chua Thanh Hóa, bánh trôi tàu"},
                {"Nghệ An", "Đặc sản từ Nghệ An - Bánh mướt, nem chua Yên Thành"},
                {"Hà Tĩnh", "Đặc sản từ Hà Tĩnh - Bánh khoái, nem nướng"},
                {"Quảng Bình", "Đặc sản từ Quảng Bình - Bánh ướt thịt nướng, tôm he"},
                {"Quảng Trị", "Đặc sản từ Quảng Trị - Bánh bèo, bánh nậm"},
                {"Thừa Thiên Huế", "Đặc sản từ Huế - Bún bò Huế, bánh khoái, chè Huế"},
                {"Đà Nẵng", "Đặc sản từ Đà Nẵng - Mì Quảng, bánh tráng cuốn thịt heo"},
                {"Quảng Nam", "Đặc sản từ Quảng Nam - Mì Quảng, bánh xèo, cao lầu"},
                {"Quảng Ngãi", "Đặc sản từ Quảng Ngãi - Bánh xèo, bánh căn"},
                {"Bình Định", "Đặc sản từ Bình Định - Bánh hỏi, bánh ít lá gai"},
                {"Phú Yên", "Đặc sản từ Phú Yên - Bánh căn, ốc hến"},
                {"Khánh Hòa", "Đặc sản từ Khánh Hòa - Bánh căn, nem nướng Nha Trang"},
                {"Ninh Thuận", "Đặc sản từ Ninh Thuận - Bánh căn, nho Ninh Thuận"},
                {"Bình Thuận", "Đặc sản từ Bình Thuận - Bánh căn, nước mắm Phan Thiết"},
                {"Kon Tum", "Đặc sản từ Kon Tum - Rượu cần, thịt nướng lá chuối"},
                {"Gia Lai", "Đặc sản từ Gia Lai - Rượu cần, bánh tráng nướng"},
                {"Đắk Lắk", "Đặc sản từ Đắk Lắk - Cà phê Buôn Ma Thuột, bánh tráng nướng"},
                {"Đắk Nông", "Đặc sản từ Đắk Nông - Cà phê, hạt điều"},
                {"Lâm Đồng", "Đặc sản từ Lâm Đồng - Rau củ Đà Lạt, bánh tráng nướng"}
        });

        addProvinceCategories(categories, "Đặc Sản Miền Nam", new String[][] {
                {"TP. Hồ Chí Minh", "Đặc sản từ TP.HCM - Bánh mì, hủ tiếu, bánh xèo"},
                {"Bà Rịa - Vũng Tàu", "Đặc sản từ Vũng Tàu - Bánh khọt, hải sản tươi sống"},
                {"Bình Dương", "Đặc sản từ Bình Dương - Bánh tráng nướng, chả cá"},
                {"Bình Phước", "Đặc sản từ Bình Phước - Hạt điều, bánh tráng"},
                {"Tây Ninh", "Đặc sản từ Tây Ninh - Bánh tráng nướng, bánh căn"},
                {"Đồng Nai", "Đặc sản từ Đồng Nai - Bánh tráng, nem nướng"},
                {"Long An", "Đặc sản từ Long An - Bánh tráng, dưa hấu"},
                {"Tiền Giang", "Đặc sản từ Tiền Giang - Bánh tráng, kẹo dừa"},
                {"Trà Vinh", "Đặc sản từ Trà Vinh - Bánh tét, bánh ít lá gai"},
                {"Vĩnh Long", "Đặc sản từ Vĩnh Long - Bánh khọt, hủ tiếu"},
                {"Đồng Tháp", "Đặc sản từ Đồng Tháp - Bánh xèo, cá linh"},
                {"An Giang", "Đặc sản từ An Giang - Bánh pía, bánh tét"},
                {"Kiên Giang", "Đặc sản từ Kiên Giang - Bánh tét, hải sản Phú Quốc"},
                {"Cần Thơ", "Đặc sản từ Cần Thơ - Bánh cống, bánh xèo"},
                {"Hậu Giang", "Đặc sản từ Hậu Giang - Bánh tét, bánh ít"},
                {"Sóc Trăng", "Đặc sản từ Sóc Trăng - Bánh pía, bánh tét"},
                {"Bạc Liêu", "Đặc sản từ Bạc Liêu - Bánh tét, tôm khô"},
                {"Cà Mau", "Đặc sản từ Cà Mau - U tôm, bánh tét"}
        });

        Supplier supplier = createOrUpdateSupplier();
        createOrUpdateRailwayProduct("Bánh Chưng Truyền Thống",
                "<p>Bánh chưng làm từ gạo nếp, đậu xanh và thịt heo, gói lá dong</p>",
                "Bánh chưng xanh truyền thống",
                "Bánh chưng gói lá dong, nhân đậu xanh thịt heo, phù hợp làm quà biếu ngày lễ Tết.",
                new BigDecimal("150000.00"), 50, categories.get("Đặc Sản Miền Bắc"), supplier,
                "/uploads/products/5185ab6c-5761-4e43-a172-805c43eb0658.jpg", "Hà Nội", 500, true);
        createOrUpdateRailwayProduct("Bánh Chưng Truyền Thống Đốc",
                "<p>Bánh chưng làm từ gạo nếp, đậu xanh và thịt heo, gói lá dong</p>",
                null, "", new BigDecimal("150000.00"), 48, categories.get("Đặc Sản Miền Bắc"), supplier,
                "/uploads/products/5185ab6c-5761-4e43-a172-805c43eb0658.jpg", "Hà Nội", 500, false);
        createOrUpdateRailwayProduct("Nem Chua Thanh Hóa",
                "<p>Nem chua truyền thống Thanh Hóa với hương vị đặc trưng</p>",
                null, "", new BigDecimal("80000.00"), 98, categories.get("Nem Chua"), supplier,
                "https://vifoodshop.com/wp-content/uploads/2019/07/nem-chua-chuan-thanh-hoa-247x296.jpg", "Thanh Hóa", 200, false);
        createOrUpdateRailwayProduct("Bánh Tráng Nướng Đà Lạt",
                "<p>Bánh tráng nướng giòn rụm với các loại topping đa dạng</p>",
                null, "", new BigDecimal("45000.00"), 200, categories.get("Đặc Sản Miền Trung"), supplier,
                "/uploads/products/73b72354-b7f0-40cb-b6ec-a94ccc466625.jpg", "Đà Lạt", 100, false);
        createOrUpdateRailwayProduct("Kẹo Dừa Bến Tre",
                "<p>Kẹo dừa thơm ngon làm từ dừa tươi Bến Tre</p>",
                null, "", new BigDecimal("60000.00"), 149, categories.get("Bánh Kẹo"), supplier,
                "/uploads/products/e8648088-122d-47f7-bd42-a1f1aeee3989.jpg", "Bến Tre", 250, false);
        createOrUpdateRailwayProduct("Chả Cá Lã Vọng",
                "<p>Chả cá truyền thống Hà Nội với hương vị đặc biệt</p>",
                null, "", new BigDecimal("200000.00"), 29, categories.get("Đặc Sản Miền Bắc"), supplier,
                "/uploads/products/3fbd6d5d-27a6-4ed9-9fb2-ba01e311bd28.jpg", "Hà Nội", 300, false);
        createOrUpdateRailwayProduct("Bánh Ít Lá Gai",
                "<p>Bánh ít lá gai truyền thống miền Trung với nhân tôm thịt</p>",
                null, "", new BigDecimal("35000.00"), 72, categories.get("Đặc Sản Miền Trung"), supplier,
                "/uploads/products/15e1ac5e-17db-43f8-9c7d-0227a82791a9.jpg", "Huế", 150, true);
        createOrUpdateRailwayProduct("Mắm Ruốc Huế",
                "<p>Mắm ruốc đặc sản Huế với hương vị đậm đà</p>",
                null, "", new BigDecimal("120000.00"), 59, categories.get("Đặc Sản Miền Trung"), supplier,
                "/uploads/products/4057446a-de2b-488e-86d7-3b70c75d0863.jpg", "Huế", 200, false);
        createOrUpdateRailwayProduct("Bánh Căn Phan Thiết",
                "<p>Bánh căn nướng giòn với nước chấm đặc biệt</p>",
                null, "", new BigDecimal("25000.00"), 117, categories.get("Đặc Sản Miền Nam"), supplier,
                "/uploads/products/142f1909-4bfa-4275-b0d1-cbddbf297da5.jpeg", "Phan Thiết", 80, false);
        createOrUpdateRailwayProduct("Nước Mắm Phan Thiết",
                "Nước mắm Phan Thiết ủ chượp truyền thống, độ đạm cao",
                "Nước mắm truyền thống Phan Thiết",
                "Nước mắm được ủ từ cá cơm tươi theo phương pháp truyền thống tại Phan Thiết.",
                new BigDecimal("85000.00"), 120, categories.get("Bình Thuận"), supplier,
                "/uploads/products/6cd8b125-7eab-4a8a-b3f7-33df51bc05d8.jpg", "Bình Thuận", 500, true);
        createOrUpdateRailwayProduct("Bánh Khọt Vũng Tàu",
                "<p>Bánh khọt Vũng Tàu giòn rụm, ăn kèm rau sống</p>",
                "Bánh khọt đặc sản Vũng Tàu",
                "<p>Bánh khọt là món ăn nổi tiếng của Vũng Tàu với lớp vỏ giòn và nhân tôm.</p>",
                new BigDecimal("60000.00"), 80, categories.get("Bà Rịa - Vũng Tàu"), supplier,
                "/uploads/products/46440fd4-274e-4243-9a0a-5e9bfda125d3.jpg", "Bà Rịa - Vũng Tàu", 400, false);
        createOrUpdateRailwayProduct("Cơm Cháy Ninh Bình",
                "Cơm cháy Ninh Bình giòn rụm, sốt đậm đà",
                "Cơm cháy đặc sản Ninh Bình",
                "Cơm cháy được chiên giòn và ăn kèm nước sốt đặc trưng của vùng Ninh Bình.",
                new BigDecimal("70000.00"), 100, categories.get("Ninh Bình"), supplier,
                "/uploads/products/01c5797f-e43f-4f33-b713-33e68528250a.webp", "Ninh Bình", 300, true);
        createOrUpdateRailwayProduct("Mắm Cá Linh An Giang",
                "Mắm cá linh An Giang - đặc sản mùa nước nổi",
                "Mắm cá linh miền Tây",
                "Mắm cá linh được làm từ cá linh tươi trong mùa nước nổi An Giang.",
                new BigDecimal("95000.00"), 60, categories.get("An Giang"), supplier,
                "/uploads/products/ece8f68a-14be-4306-a3fa-47d4d320a6cc.jpg", "An Giang", 500, false);
        createOrUpdateRailwayProduct("Hải Sản Khô Cà Mau",
                "Hải sản khô Cà Mau - tôm khô, cá khô tự nhiên",
                "Hải sản khô đặc sản Cà Mau",
                "Hải sản được phơi khô tự nhiên tại vùng biển Cà Mau.",
                new BigDecimal("150000.00"), 49, categories.get("Cà Mau"), supplier,
                "/uploads/products/46440fd4-274e-4243-9a0a-5e9bfda125d3.jpg", "Cà Mau", 600, true);

        System.out.println("✅ DacSanVietRail catalog data synchronized successfully!");
    }

    private void addProvinceCategories(Map<String, Category> categories, String parentName, String[][] data) {
        Category parent = categories.get(parentName);
        for (String[] item : data) {
            categories.put(item[0], createOrUpdateCategory(item[0], item[1], null, parent));
        }
    }

    private Category createOrUpdateCategory(String name, String description, String imageUrl, Category parent) {
        Category category = categoryRepository.findByName(name).orElseGet(Category::new);
        category.setName(name);
        category.setDescription(description);
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        }
        category.setParent(parent);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    private Supplier createOrUpdateSupplier() {
        Supplier supplier = supplierRepository.findAll().stream()
                .filter(s -> "Công ty TNHH Khoga Detem".equals(s.getName()))
                .findFirst()
                .orElseGet(Supplier::new);
        supplier.setName("Công ty TNHH Khoga Detem");
        supplier.setContactPerson("Phùng Đô Thạnh");
        supplier.setPhone("0328494207");
        supplier.setEmail("phungdothanh@khogadetem.com");
        supplier.setAddress("120 Yên Lãng, Phường Kim Liên, Quận Đống Đa, Thành phố Hà Nội");
        supplier.setTaxCode("75837583758378");
        supplier.setIsActive(true);
        return supplierRepository.save(supplier);
    }

    private Product createOrUpdateRailwayProduct(String name, String description, String shortDescription, String story,
                                                BigDecimal price, Integer stock, Category category, Supplier supplier,
                                                String imageUrl, String origin, Integer weight, boolean featured) {
        if (category == null) {
            logger.warn("Category not found for product {}, skipping catalog sync", name);
            return null;
        }

        Product product = productRepository.findByName(name).orElseGet(Product::new);
        product.setName(name);
        product.setDescription(description);
        product.setShortDescription(shortDescription);
        product.setStory(story);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setImageUrl(imageUrl);
        product.setOrigin(origin);
        product.setWeightGrams(weight);
        product.setIsActive(true);
        product.setIsFeatured(featured);
        return productRepository.save(product);
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
