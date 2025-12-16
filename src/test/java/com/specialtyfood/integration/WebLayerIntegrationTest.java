package com.specialtyfood.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.specialtyfood.dto.*;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import com.specialtyfood.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Web layer integration test covering end-to-end workflows, 
 * cross-browser compatibility simulation, and mobile responsiveness
 * Validates Requirements: All requirements validation
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class WebLayerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    private MockMvc mockMvc;
    private User testUser;
    private User adminUser;
    private Category testCategory;
    private Product testProduct1;
    private Product testProduct2;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Create test category
        testCategory = new Category();
        testCategory.setName("Đặc sản miền Bắc");
        testCategory.setDescription("Các sản phẩm đặc sản từ miền Bắc Việt Nam");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Bánh chưng Hà Nội");
        testProduct1.setDescription("Bánh chưng truyền thống Hà Nội");
        testProduct1.setPrice(new BigDecimal("150000"));
        testProduct1.setStockQuantity(100);
        testProduct1.setIsActive(true);
        testProduct1.setIsFeatured(true);
        testProduct1.setCategory(testCategory);
        testProduct1.setImageUrl("/images/banh-chung.jpg");
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Chả cá Lã Vọng");
        testProduct2.setDescription("Chả cá truyền thống Hà Nội");
        testProduct2.setPrice(new BigDecimal("200000"));
        testProduct2.setStockQuantity(50);
        testProduct2.setIsActive(true);
        testProduct2.setIsFeatured(false);
        testProduct2.setCategory(testCategory);
        testProduct2.setImageUrl("/images/cha-ca.jpg");
        testProduct2 = productRepository.save(testProduct2);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6"); // "password"
        testUser.setFullName("Nguyễn Văn Test");
        testUser.setPhoneNumber(null);
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6"); // "password"
        adminUser.setFullName("Admin User");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        // Generate JWT tokens
        userToken = jwtTokenProvider.generateTokenFromUsername(testUser.getUsername());
        adminToken = jwtTokenProvider.generateTokenFromUsername(adminUser.getUsername());
    }

    @Test
    @Order(1)
    void testHomepageEndToEnd() throws Exception {
        // Test homepage displays featured products (Requirement 1.1)
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("featuredProducts"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @Order(2)
    void testProductBrowsingWorkflow() throws Exception {
        // Test product listing page (Requirement 1.1)
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));

        // Test product search (Requirement 1.2)
        mockMvc.perform(get("/products").param("search", "Bánh"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"));

        // Test category filtering (Requirement 1.3)
        mockMvc.perform(get("/products").param("categoryId", testCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"));

        // Test product detail page (Requirement 1.4)
        mockMvc.perform(get("/products/" + testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @Order(3)
    void testShoppingCartWorkflow() throws Exception {
        // Test add to cart API (Requirement 2.1)
        AddToCartRequest cartRequest = new AddToCartRequest(testProduct1.getId(), 2);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(2));

        // Test view cart page (Requirement 2.2)
        mockMvc.perform(get("/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/index"))
                .andExpect(model().attributeExists("cart"));

        // Test update cart API (Requirement 2.3)
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(testProduct1.getId(), 3);
        
        mockMvc.perform(put("/api/cart/update")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3));
    }

    @Test
    @Order(4)
    void testOrderWorkflow() throws Exception {
        // Setup: Add items to cart
        AddToCartRequest cartRequest = new AddToCartRequest(testProduct1.getId(), 2);
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)));

        // Create address
        Address address = new Address();
        address.setFullName("Test User");
        address.setAddressLine1("123 Test Street");
        address.setCity("Test City");
        address.setProvince("Test Province");
        address.setPostalCode("12345");
        address.setCountry("Vietnam");
        address.setUser(testUser);
        address = addressRepository.save(address);

        // Test create order API (Requirement 2.5)
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setShippingAddressId(address.getId());
        orderRequest.setPaymentMethod("Credit Card");
        
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderItems").isArray());

        // Test view orders page (Requirement 3.4)
        mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @Order(5)
    void testAdminWorkflow() throws Exception {
        // Test admin dashboard (Requirement 5.1)
        mockMvc.perform(get("/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("dashboardMetrics"));

        // Test admin product management (Requirement 4.1)
        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("New Product");
        productRequest.setDescription("New Product Description");
        productRequest.setPrice(new BigDecimal("100000"));
        productRequest.setStockQuantity(50);
        productRequest.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"));

        // Test admin order management (Requirement 5.2)
        mockMvc.perform(get("/admin/orders")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @Order(6)
    void testCrossBrowserCompatibilitySimulation() throws Exception {
        // Simulate different browser user agents and test responses
        
        // Test with Chrome user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test with Firefox user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test with Safari user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test with Edge user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test API endpoints work consistently across browsers
        mockMvc.perform(get("/api/products")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(7)
    void testMobileResponsivenessSimulation() throws Exception {
        // Test with mobile user agents and viewport settings
        
        // Test with iPhone user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test with Android user agent
        mockMvc.perform(get("/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Test mobile-optimized API responses
        mockMvc.perform(get("/api/products")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)")
                .param("page", "0")
                .param("size", "5")) // Smaller page size for mobile
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));

        // Test mobile cart operations
        AddToCartRequest mobileCartRequest = new AddToCartRequest(testProduct1.getId(), 1);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileCartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @Order(8)
    void testAccessibilityFeatures() throws Exception {
        // Test that pages include proper accessibility attributes
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lang="))) // Language attribute
                .andExpect(content().string(containsString("alt="))) // Alt text for images
                .andExpect(content().string(containsString("aria-"))); // ARIA attributes

        // Test product pages have proper semantic structure
        mockMvc.perform(get("/products/" + testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1"))) // Proper heading structure
                .andExpect(content().string(containsString("role="))) // ARIA roles
                .andExpect(content().string(containsString("tabindex="))); // Keyboard navigation
    }

    @Test
    @Order(9)
    void testErrorHandlingAndRecovery() throws Exception {
        // Test 404 error handling
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound());

        // Test invalid API requests
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 999999, \"quantity\": 1}"))
                .andExpect(status().isBadRequest());

        // Test unauthorized access
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        // Test system recovery after errors
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void testPerformanceAndLoadHandling() throws Exception {
        // Test that pages load within reasonable time
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within 2 seconds
        assert duration < 2000 : "Homepage took too long to load: " + duration + "ms";

        // Test API performance
        startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
        
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        
        // API should be faster
        assert duration < 1000 : "API took too long to respond: " + duration + "ms";
    }
}
  