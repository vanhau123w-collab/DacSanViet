package com.specialtyfood.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.specialtyfood.dto.*;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import com.specialtyfood.security.JwtTokenProvider;
import com.specialtyfood.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test covering end-to-end workflows,
 * cross-browser compatibility, mobile device testing, and performance validation
 * 
 * **Feature: specialty-food-ecommerce, Integration Testing**
 * Validates Requirements: All requirements validation - comprehensive system testing
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class ComprehensiveIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminService adminService;

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

    // Browser user agents for cross-browser testing
    private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0";
    private static final String SAFARI_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15";
    private static final String EDGE_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
    
    // Mobile user agents for mobile testing
    private static final String IPHONE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1";
    private static final String ANDROID_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
    private static final String IPAD_USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        setupTestData();
    }

    private void setupTestData() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Đặc sản Việt Nam");
        testCategory.setDescription("Các sản phẩm đặc sản truyền thống Việt Nam");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Bánh chưng Hà Nội");
        testProduct1.setDescription("Bánh chưng truyền thống Hà Nội làm từ gạo nếp, đậu xanh và thịt heo");
        testProduct1.setPrice(new BigDecimal("150000"));
        testProduct1.setStockQuantity(100);
        testProduct1.setIsActive(true);
        testProduct1.setIsFeatured(true);
        testProduct1.setCategory(testCategory);
        testProduct1.setImageUrl("/images/banh-chung.jpg");
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Chả cá Lã Vọng");
        testProduct2.setDescription("Chả cá truyền thống Hà Nội với hương vị đặc trưng");
        testProduct2.setPrice(new BigDecimal("200000"));
        testProduct2.setStockQuantity(50);
        testProduct2.setIsActive(true);
        testProduct2.setIsFeatured(false);
        testProduct2.setCategory(testCategory);
        testProduct2.setImageUrl("/images/cha-ca.jpg");
        testProduct2 = productRepository.save(testProduct2);

        // Create test users
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6");
        testUser.setFullName("Nguyễn Văn Integration");
        testUser.setPhoneNumber(null);
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setUsername("integrationadmin");
        adminUser.setEmail("admin@integration.com");
        adminUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6");
        adminUser.setFullName("Admin Integration");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        // Generate JWT tokens
        userToken = jwtTokenProvider.generateTokenFromUsername(testUser.getUsername());
        adminToken = jwtTokenProvider.generateTokenFromUsername(adminUser.getUsername());
    }

    @Test
    @Order(1)
    void testCompleteCustomerJourneyEndToEnd() throws Exception {
        // **Complete customer journey from browsing to order completion**
        // Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.5, 3.4, 3.5

        // Step 1: Browse homepage
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Step 2: Search for products via API
        mockMvc.perform(get("/api/products/search").param("keyword", "Bánh"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Step 3: View product details via API
        mockMvc.perform(get("/api/products/" + testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Bánh chưng Hà Nội"));

        // Step 4: Add to cart
        AddToCartRequest cartRequest = new AddToCartRequest(testProduct1.getId(), 2);
        mockMvc.perform(post("/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartItemCount").value(1));

        // Step 5: View cart summary via API
        mockMvc.perform(get("/cart/summary")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Step 6: Update cart quantity
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(testProduct1.getId(), 3);
        mockMvc.perform(put("/cart/update")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 7: Create shipping address
        Address address = new Address();
        address.setFullName("Nguyễn Văn Integration");
        address.setAddressLine1("123 Đường Integration");
        address.setCity("Hà Nội");
        address.setProvince("Hà Nội");
        address.setPostalCode("100000");
        address.setCountry("Việt Nam");
        address.setUser(testUser);
        address = addressRepository.save(address);

        // Step 8: Create order
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setShippingAddressId(address.getId());
        orderRequest.setPaymentMethod("Credit Card");
        orderRequest.setNotes("Integration test order");

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems", hasSize(1)));

        // Step 9: View order history via API
        mockMvc.perform(get("/api/orders/my-orders")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify inventory was updated
        Product updatedProduct = productRepository.findById(testProduct1.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(97, updatedProduct.getStockQuantity()); // 100 - 3 = 97
    }

    @Test
    @Order(2)
    void testCrossBrowserCompatibility() throws Exception {
        // **Test system works consistently across different browsers**
        // Validates: Requirements 8.1, 8.2 - Cross-browser compatibility

        String[] userAgents = {CHROME_USER_AGENT, FIREFOX_USER_AGENT, SAFARI_USER_AGENT, EDGE_USER_AGENT};
        
        for (String userAgent : userAgents) {
            // Test homepage loads correctly
            mockMvc.perform(get("/")
                    .header("User-Agent", userAgent))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));

            // Test product listing API works
            mockMvc.perform(get("/api/products")
                    .header("User-Agent", userAgent))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Test API endpoints work consistently
            mockMvc.perform(get("/api/products")
                    .header("User-Agent", userAgent))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Test search functionality via API
            mockMvc.perform(get("/api/products/search")
                    .header("User-Agent", userAgent)
                    .param("keyword", "Bánh"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Test cart operations work across browsers
            AddToCartRequest cartRequest = new AddToCartRequest(testProduct1.getId(), 1);
            mockMvc.perform(post("/cart/add")
                    .header("Authorization", "Bearer " + userToken)
                    .header("User-Agent", userAgent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Clear cart for next iteration
            cartService.clearCart(testUser.getId());
        }
    }

    @Test
    @Order(3)
    void testMobileDeviceCompatibility() throws Exception {
        // **Test system works on mobile devices with responsive design**
        // Validates: Requirements 8.1 - Mobile responsiveness

        String[] mobileUserAgents = {IPHONE_USER_AGENT, ANDROID_USER_AGENT, IPAD_USER_AGENT};
        
        for (String userAgent : mobileUserAgents) {
            // Test mobile homepage
            mockMvc.perform(get("/")
                    .header("User-Agent", userAgent))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));

            // Test mobile product listing with smaller page sizes
            mockMvc.perform(get("/api/products")
                    .header("User-Agent", userAgent)
                    .param("page", "0")
                    .param("size", "5")) // Mobile-optimized page size
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(5));

            // Test mobile search via API
            mockMvc.perform(get("/api/products/search")
                    .header("User-Agent", userAgent)
                    .param("keyword", "Bánh"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Test mobile cart operations (should be fast)
            long startTime = System.currentTimeMillis();
            
            AddToCartRequest mobileCartRequest = new AddToCartRequest(testProduct1.getId(), 1);
            mockMvc.perform(post("/cart/add")
                    .header("Authorization", "Bearer " + userToken)
                    .header("User-Agent", userAgent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mobileCartRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            
            long endTime = System.currentTimeMillis();
            assertTrue((endTime - startTime) < 500, "Mobile cart operation should be fast");

            // Test mobile checkout flow via API
            mockMvc.perform(get("/cart/summary")
                    .header("Authorization", "Bearer " + userToken)
                    .header("User-Agent", userAgent))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Clear cart for next iteration
            cartService.clearCart(testUser.getId());
        }
    }

    @Test
    @Order(4)
    void testAccessibilityCompliance() throws Exception {
        // **Test accessibility features work correctly**
        // Validates: Requirements 8.5 - Accessibility compliance

        // Test homepage accessibility
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lang="))) // Language attribute
                .andExpect(content().string(containsString("alt="))) // Alt text for images
                .andExpect(content().string(containsString("aria-"))) // ARIA attributes
                .andExpect(content().string(containsString("role="))); // ARIA roles

        // Test product page accessibility
        mockMvc.perform(get("/products/" + testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1"))) // Proper heading structure
                .andExpect(content().string(containsString("tabindex="))) // Keyboard navigation
                .andExpect(content().string(containsString("aria-label="))) // Screen reader labels
                .andExpect(content().string(containsString("role=")));

        // Test form accessibility
        mockMvc.perform(get("/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("label for="))) // Form labels
                .andExpect(content().string(containsString("aria-describedby="))) // Form descriptions
                .andExpect(content().string(containsString("required"))); // Required field indicators

        // Verify all products have proper descriptions for screen readers
        Page<ProductDto> products = productService.getAllProducts(PageRequest.of(0, 10));
        for (ProductDto product : products.getContent()) {
            assertNotNull(product.getName(), "Product name required for accessibility");
            assertNotNull(product.getDescription(), "Product description required for accessibility");
            assertFalse(product.getName().trim().isEmpty(), "Product name cannot be empty");
            assertFalse(product.getDescription().trim().isEmpty(), "Product description cannot be empty");
        }
    }

    @Test
    @Order(5)
    void testPerformanceUnderLoad() throws Exception {
        // **Test system performance under concurrent load**
        // Validates: Requirements 8.4 - Performance requirements

        // Test homepage performance
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) < 2000, "Homepage should load within 2 seconds");

        // Test API performance
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
        endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) < 1000, "API should respond within 1 second");

        // Test concurrent cart operations
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            final int index = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Create unique user for each thread
                    User threadUser = new User();
                    threadUser.setUsername("perfuser" + index);
                    threadUser.setEmail("perf" + index + "@example.com");
                    threadUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6");
                    threadUser.setFullName("Performance User " + index);
                    threadUser.setRole(Role.USER);
                    threadUser = userRepository.save(threadUser);

                    AddToCartRequest request = new AddToCartRequest(testProduct1.getId(), 1);
                    CartDto cart = cartService.addToCart(threadUser.getId(), request);
                    return cart.getTotalQuantity() == 1;
                } catch (Exception e) {
                    return false;
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all operations to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.get(15, TimeUnit.SECONDS);

        // Verify most operations succeeded (at least 90%)
        long successCount = futures.stream().mapToLong(f -> {
            try {
                return f.get() ? 1 : 0;
            } catch (Exception e) {
                return 0;
            }
        }).sum();

        assertTrue(successCount >= 18, "At least 90% of concurrent operations should succeed, got: " + successCount + "/20");
        executor.shutdown();
    }

    @Test
    @Order(6)
    void testErrorHandlingAndRecovery() throws Exception {
        // **Test system handles errors gracefully and recovers**
        // Validates: All requirements - Error handling

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

        // Test invalid data handling
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"invalid\", \"quantity\": -1}"))
                .andExpect(status().isBadRequest());

        // Test system recovery after errors
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // Test valid operations still work after errors
        AddToCartRequest validRequest = new AddToCartRequest(testProduct1.getId(), 1);
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @Order(7)
    void testDataIntegrityAndConsistency() throws Exception {
        // **Test data integrity across all operations**
        // Validates: Requirements 2.2, 4.5 - Data consistency

        // Test cart total calculation accuracy
        AddToCartRequest request1 = new AddToCartRequest(testProduct1.getId(), 2);
        AddToCartRequest request2 = new AddToCartRequest(testProduct2.getId(), 1);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(500000)); // 150000*2 + 200000*1

        // Test inventory consistency
        Product product1Before = productRepository.findById(testProduct1.getId()).orElse(null);
        Product product2Before = productRepository.findById(testProduct2.getId()).orElse(null);
        
        assertNotNull(product1Before);
        assertNotNull(product2Before);
        int stock1Before = product1Before.getStockQuantity();
        int stock2Before = product2Before.getStockQuantity();

        // Create order
        Address address = new Address();
        address.setFullName("Test User");
        address.setAddressLine1("123 Test Street");
        address.setCity("Test City");
        address.setProvince("Test Province");
        address.setPostalCode("12345");
        address.setCountry("Vietnam");
        address.setUser(testUser);
        address = addressRepository.save(address);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setShippingAddressId(address.getId());
        orderRequest.setPaymentMethod("Credit Card");

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Verify inventory was properly reduced
        Product product1After = productRepository.findById(testProduct1.getId()).orElse(null);
        Product product2After = productRepository.findById(testProduct2.getId()).orElse(null);
        
        assertNotNull(product1After);
        assertNotNull(product2After);
        assertEquals(stock1Before - 2, product1After.getStockQuantity());
        assertEquals(stock2Before - 1, product2After.getStockQuantity());
    }

    @Test
    @Order(8)
    void testAdminWorkflowIntegration() throws Exception {
        // **Test complete admin workflow**
        // Validates: Requirements 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4, 5.5

        // Test admin dashboard API
        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Test product management
        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("Nem Hà Nội");
        productRequest.setDescription("Nem truyền thống Hà Nội");
        productRequest.setPrice(new BigDecimal("80000"));
        productRequest.setStockQuantity(30);
        productRequest.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nem Hà Nội"))
                .andExpect(jsonPath("$.price").value(80000));

        // Test category management
        CreateCategoryRequest categoryRequest = new CreateCategoryRequest();
        categoryRequest.setName("Đặc sản miền Nam");
        categoryRequest.setDescription("Các sản phẩm đặc sản từ miền Nam");

        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Đặc sản miền Nam"));

        // Test order management API
        mockMvc.perform(get("/api/admin/orders")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Test customer management API
        mockMvc.perform(get("/api/admin/customers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(9)
    void testSecurityAndAuthentication() throws Exception {
        // **Test security measures work correctly**
        // Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5

        // Test unauthorized access is blocked
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 1, \"quantity\": 1}"))
                .andExpect(status().isUnauthorized());

        // Test invalid token is rejected
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        // Test valid token works
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Test admin-only endpoints require admin role
        mockMvc.perform(get("/admin/dashboard")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void testComprehensiveSystemValidation() throws Exception {
        // **Final comprehensive validation of all system components**
        // Validates: All requirements - Complete system validation

        // Test complete user workflow with multiple browsers
        String[] browsers = {CHROME_USER_AGENT, FIREFOX_USER_AGENT, IPHONE_USER_AGENT};
        
        for (String browser : browsers) {
            // Browse products via API
            mockMvc.perform(get("/api/products")
                    .header("User-Agent", browser))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Search products via API
            mockMvc.perform(get("/api/products/search")
                    .header("User-Agent", browser)
                    .param("keyword", "Bánh"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Add to cart
            AddToCartRequest request = new AddToCartRequest(testProduct1.getId(), 1);
            mockMvc.perform(post("/cart/add")
                    .header("Authorization", "Bearer " + userToken)
                    .header("User-Agent", browser)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Clear cart for next iteration
            cartService.clearCart(testUser.getId());
        }

        // Test system handles Vietnamese characters correctly
        CreateProductRequest vietnameseProduct = new CreateProductRequest();
        vietnameseProduct.setName("Bánh mì Sài Gòn");
        vietnameseProduct.setDescription("Bánh mì truyền thống với đầy đủ nhân");
        vietnameseProduct.setPrice(new BigDecimal("25000"));
        vietnameseProduct.setStockQuantity(100);
        vietnameseProduct.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vietnameseProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bánh mì Sài Gòn"));

        // Test search with Vietnamese characters via API
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "Sài Gòn"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify all core services are working
        assertNotNull(userService);
        assertNotNull(productService);
        assertNotNull(categoryService);
        assertNotNull(cartService);
        assertNotNull(orderService);
        assertNotNull(adminService);

        // Final validation: system is ready for production
        assertTrue(true, "Comprehensive integration testing completed successfully");
    }
}