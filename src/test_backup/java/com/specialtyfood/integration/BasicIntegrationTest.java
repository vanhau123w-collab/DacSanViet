package com.specialtyfood.integration;

import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import com.specialtyfood.service.*;
import com.specialtyfood.dao.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic integration test for core system functionality
 * Tests end-to-end workflows without web layer complexity
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BasicIntegrationTest {

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
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    private User testUser;
    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Integration Test Category");
        testCategory.setDescription("Category for integration testing");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Integration Test Product");
        testProduct.setDescription("Product for integration testing");
        testProduct.setPrice(new BigDecimal("100000"));
        testProduct.setStockQuantity(50);
        testProduct.setIsActive(true);
        testProduct.setIsFeatured(true);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);

        // Create test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjO6NaaJUPL9xzMWlV6H6"); // "password"
        testUser.setFullName("Integration Test User");
        testUser.setPhoneNumber(null);
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCompleteUserWorkflow() {
        // Test 1: User can browse products
        Page<ProductDao> products = productService.getAllProducts(PageRequest.of(0, 10));
        assertFalse(products.isEmpty());
        assertTrue(products.getContent().stream().anyMatch(p -> p.getName().equals("Integration Test Product")));

        // Test 2: User can search products
        Page<ProductDao> searchResults = productService.searchProducts("Integration", PageRequest.of(0, 10));
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.getContent().stream().anyMatch(p -> p.getName().contains("Integration")));

        // Test 3: User can get product details
        ProductDao productDetails = productService.getProductById(testProduct.getId());
        assertNotNull(productDetails);
        assertEquals("Integration Test Product", productDetails.getName());
        assertEquals(new BigDecimal("100000"), productDetails.getPrice());

        // Test 4: User can add product to cart
        AddToCartRequest cartRequest = new AddToCartRequest(testProduct.getId(), 2);
        CartDao cart = cartService.addToCart(testUser.getId(), cartRequest);
        
        assertNotNull(cart);
        assertEquals(1, cart.getTotalItems());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(new BigDecimal("200000"), cart.getTotalAmount());

        // Test 5: User can view cart
        CartDao retrievedCart = cartService.getCart(testUser.getId());
        assertEquals(cart.getTotalItems(), retrievedCart.getTotalItems());
        assertEquals(cart.getTotalQuantity(), retrievedCart.getTotalQuantity());
        assertEquals(cart.getTotalAmount(), retrievedCart.getTotalAmount());

        // Test 6: User can update cart
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(testProduct.getId(), 3);
        CartDao updatedCart = cartService.updateCartItem(testUser.getId(), updateRequest);
        assertEquals(3, updatedCart.getTotalQuantity());
        assertEquals(new BigDecimal("300000"), updatedCart.getTotalAmount());

        // Test 7: User can create address
        Address address = new Address();
        address.setFullName("Integration Test User");
        address.setAddressLine1("123 Integration Street");
        address.setCity("Test City");
        address.setProvince("Test Province");
        address.setPostalCode("12345");
        address.setCountry("Vietnam");
        address.setUser(testUser);
        address = addressRepository.save(address);

        // Test 8: User can create order
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setShippingAddressId(address.getId());
        orderRequest.setPaymentMethod("Credit Card");
        orderRequest.setNotes("Integration test order");

        OrderDao order = orderService.createOrder(testUser.getId(), orderRequest);
        
        assertNotNull(order);
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals("Credit Card", order.getPaymentMethod());
        assertEquals(1, order.getOrderItems().size());

        // Test 9: Verify inventory was updated
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(47, updatedProduct.getStockQuantity()); // 50 - 3 = 47

        // Test 10: Verify cart was cleared
        CartDao clearedCart = cartService.getCart(testUser.getId());
        assertEquals(0, clearedCart.getTotalItems());
        assertEquals(0, clearedCart.getTotalQuantity());

        // Test 11: User can view order
        OrderDao retrievedOrder = orderService.getOrderById(order.getId());
        assertEquals(order.getId(), retrievedOrder.getId());
        assertEquals(order.getOrderNumber(), retrievedOrder.getOrderNumber());
    }

    @Test
    void testCategoryManagement() {
        // Test category creation
        List<CategoryDao> categories = categoryService.getAllActiveCategories();
        assertFalse(categories.isEmpty());
        
        CategoryDao category = categories.stream()
                .filter(c -> c.getName().equals("Integration Test Category"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(category);
        assertEquals("Integration Test Category", category.getName());
        assertEquals("Category for integration testing", category.getDescription());

        // Test products by category
        Page<ProductDao> categoryProducts = productService.getProductsByCategory(testCategory.getId(), PageRequest.of(0, 10));
        assertFalse(categoryProducts.isEmpty());
        assertTrue(categoryProducts.getContent().stream().allMatch(p -> p.getCategoryId().equals(testCategory.getId())));
    }

    @Test
    void testInventoryManagement() {
        // Test initial stock
        ProductDao product = productService.getProductById(testProduct.getId());
        assertEquals(50, product.getStockQuantity());

        // Test stock reduction through cart operations
        AddToCartRequest cartRequest = new AddToCartRequest(testProduct.getId(), 5);
        cartService.addToCart(testUser.getId(), cartRequest);

        // Create address and order
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

        OrderDao order = orderService.createOrder(testUser.getId(), orderRequest);
        assertNotNull(order);

        // Verify stock was reduced
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(45, updatedProduct.getStockQuantity()); // 50 - 5 = 45

        // Test order cancellation restores stock
        OrderDao cancelledOrder = orderService.cancelOrder(order.getId(), testUser.getId());
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        // Verify stock was restored
        Product restoredProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(restoredProduct);
        assertEquals(50, restoredProduct.getStockQuantity()); // Back to 50
    }

    @Test
    void testErrorHandling() {
        // Test adding non-existent product to cart
        AddToCartRequest invalidRequest = new AddToCartRequest(999L, 1);
        assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(testUser.getId(), invalidRequest);
        });

        // Test adding more items than available stock
        AddToCartRequest oversellRequest = new AddToCartRequest(testProduct.getId(), 100);
        assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(testUser.getId(), oversellRequest);
        });

        // Test creating order with empty cart
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setShippingAddressId(1L); // Assuming this exists
        orderRequest.setPaymentMethod("Credit Card");

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testUser.getId(), orderRequest);
        });
    }

    @Test
    void testDataIntegrity() {
        // Test that user data is properly stored and retrieved
        User retrievedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(retrievedUser);
        assertEquals("integrationuser", retrievedUser.getUsername());
        assertEquals("integration@example.com", retrievedUser.getEmail());
        assertEquals("Integration Test User", retrievedUser.getFullName());

        // Test that product data is properly stored and retrieved
        Product retrievedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(retrievedProduct);
        assertEquals("Integration Test Product", retrievedProduct.getName());
        assertEquals(new BigDecimal("100000"), retrievedProduct.getPrice());
        assertEquals(testCategory.getId(), retrievedProduct.getCategory().getId());

        // Test that category data is properly stored and retrieved
        Category retrievedCategory = categoryRepository.findById(testCategory.getId()).orElse(null);
        assertNotNull(retrievedCategory);
        assertEquals("Integration Test Category", retrievedCategory.getName());
        assertTrue(retrievedCategory.getIsActive());
    }

    @Test
    void testBusinessLogicValidation() {
        // Test that inactive products cannot be added to cart
        testProduct.setIsActive(false);
        productRepository.save(testProduct);

        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 1);
        assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(testUser.getId(), request);
        });

        // Restore product for other tests
        testProduct.setIsActive(true);
        productRepository.save(testProduct);

        // Test that cart totals are calculated correctly
        AddToCartRequest validRequest = new AddToCartRequest(testProduct.getId(), 3);
        CartDao cart = cartService.addToCart(testUser.getId(), validRequest);

        assertEquals(1, cart.getTotalItems()); // 1 unique product
        assertEquals(3, cart.getTotalQuantity()); // 3 total items
        assertEquals(new BigDecimal("300000"), cart.getTotalAmount()); // 100000 * 3

        // Test adding same product again increases quantity
        AddToCartRequest additionalRequest = new AddToCartRequest(testProduct.getId(), 2);
        CartDao updatedCart = cartService.addToCart(testUser.getId(), additionalRequest);

        assertEquals(1, updatedCart.getTotalItems()); // Still 1 unique product
        assertEquals(5, updatedCart.getTotalQuantity()); // 3 + 2 = 5 total items
        assertEquals(new BigDecimal("500000"), updatedCart.getTotalAmount()); // 100000 * 5
    }

    @Test
    void testConcurrentOperations() {
        // Test that multiple cart operations work correctly
        AddToCartRequest request1 = new AddToCartRequest(testProduct.getId(), 2);
        CartDao cart1 = cartService.addToCart(testUser.getId(), request1);
        assertEquals(2, cart1.getTotalQuantity());

        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(testProduct.getId(), 5);
        CartDao cart2 = cartService.updateCartItem(testUser.getId(), updateRequest);
        assertEquals(5, cart2.getTotalQuantity());

        CartDao cart3 = cartService.removeFromCart(testUser.getId(), testProduct.getId());
        assertEquals(0, cart3.getTotalQuantity());
    }

    @Test
    void testSystemPerformance() {
        // Test that basic operations complete in reasonable time
        long startTime = System.currentTimeMillis();

        // Perform multiple operations
        for (int i = 0; i < 10; i++) {
            productService.getAllProducts(PageRequest.of(0, 5));
            categoryService.getAllActiveCategories();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Operations should complete within 5 seconds
        assertTrue(duration < 5000, "Basic operations took too long: " + duration + "ms");
    }
}