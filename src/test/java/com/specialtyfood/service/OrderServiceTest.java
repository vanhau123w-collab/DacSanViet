package com.specialtyfood.service;

import com.specialtyfood.dto.CreateOrderRequest;
import com.specialtyfood.dto.OrderDto;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OrderService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    private User testUser;
    private Product testProduct;
    private Address testAddress;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
        
        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test category description");
        testCategory = categoryRepository.save(testCategory);
        
        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test product description");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStockQuantity(50);
        testProduct.setIsActive(true);
        testProduct.setIsFeatured(false);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);
        
        // Create test address
        testAddress = new Address();
        testAddress.setFullName("Test User");
        testAddress.setAddressLine1("123 Test Street");
        testAddress.setCity("Test City");
        testAddress.setProvince("Test Province");
        testAddress.setPostalCode("12345");
        testAddress.setCountry("Vietnam");
        testAddress.setUser(testUser);
        testAddress = addressRepository.save(testAddress);
        
        // Add item to cart
        CartItem cartItem = new CartItem();
        cartItem.setUser(testUser);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(testProduct.getPrice());
        cartItemRepository.save(cartItem);
    }
    
    @Test
    void testCreateOrder() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddressId(testAddress.getId());
        request.setPaymentMethod("Credit Card");
        request.setNotes("Test order notes");
        
        // When
        OrderDto result = orderService.createOrder(testUser.getId(), request);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getOrderNumber());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("Credit Card", result.getPaymentMethod());
        assertEquals("Test order notes", result.getNotes());
        
        // Check order items
        assertNotNull(result.getOrderItems());
        assertEquals(1, result.getOrderItems().size());
        assertEquals(testProduct.getId(), result.getOrderItems().get(0).getProductId());
        assertEquals(2, result.getOrderItems().get(0).getQuantity());
        
        // Check that cart is cleared
        assertEquals(0, cartItemRepository.findByUserIdOrderByAddedDateDesc(testUser.getId()).size());
        
        // Check that inventory is updated
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(48, updatedProduct.getStockQuantity()); // 50 - 2 = 48
    }
    
    @Test
    void testCreateOrderWithEmptyCart() {
        // Given - clear the cart first
        cartItemRepository.deleteByUserId(testUser.getId());
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddressId(testAddress.getId());
        request.setPaymentMethod("Credit Card");
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testUser.getId(), request);
        });
        
        assertEquals("Cart is empty", exception.getMessage());
    }
    
    @Test
    void testCreateOrderWithInsufficientStock() {
        // Given - set product stock to less than cart quantity
        testProduct.setStockQuantity(1); // Cart has 2 items, but only 1 in stock
        productRepository.save(testProduct);
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddressId(testAddress.getId());
        request.setPaymentMethod("Credit Card");
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testUser.getId(), request);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }
    
    @Test
    void testGetOrderById() {
        // Given - create an order first
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddressId(testAddress.getId());
        request.setPaymentMethod("Credit Card");
        
        OrderDto createdOrder = orderService.createOrder(testUser.getId(), request);
        
        // When
        OrderDto result = orderService.getOrderById(createdOrder.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(createdOrder.getId(), result.getId());
        assertEquals(createdOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(testUser.getId(), result.getUserId());
    }
    
    @Test
    void testCancelOrder() {
        // Given - create an order first
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddressId(testAddress.getId());
        request.setPaymentMethod("Credit Card");
        
        OrderDto createdOrder = orderService.createOrder(testUser.getId(), request);
        
        // When
        OrderDto result = orderService.cancelOrder(createdOrder.getId(), testUser.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        
        // Check that inventory is restored
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(50, updatedProduct.getStockQuantity()); // Back to original 50
    }
}