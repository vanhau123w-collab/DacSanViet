package com.specialtyfood.service;

import com.specialtyfood.dto.AddToCartRequest;
import com.specialtyfood.dto.CartDto;
import com.specialtyfood.dto.UpdateCartItemRequest;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.CartItemRepository;
import com.specialtyfood.repository.ProductRepository;
import com.specialtyfood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CartService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private com.specialtyfood.repository.CategoryRepository categoryRepository;
    
    private User testUser;
    private Product testProduct;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test category description");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
        
        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test product description");
        testProduct.setPrice(new BigDecimal("10.00"));
        testProduct.setStockQuantity(100);
        testProduct.setIsActive(true);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);
    }
    
    @Test
    void testAddToCart_NewItem() {
        // Given
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 2);
        
        // When
        CartDto cart = cartService.addToCart(testUser.getId(), request);
        
        // Then
        assertNotNull(cart);
        assertEquals(testUser.getId(), cart.getUserId());
        assertEquals(1, cart.getTotalItems());
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(new BigDecimal("20.00"), cart.getTotalAmount());
        
        // Verify cart item
        assertEquals(1, cart.getItems().size());
        assertEquals(testProduct.getId(), cart.getItems().get(0).getProduct().getId());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("10.00"), cart.getItems().get(0).getUnitPrice());
    }
    
    @Test
    void testAddToCart_ExistingItem() {
        // Given - Add item first time
        AddToCartRequest request1 = new AddToCartRequest(testProduct.getId(), 2);
        cartService.addToCart(testUser.getId(), request1);
        
        // When - Add same item again
        AddToCartRequest request2 = new AddToCartRequest(testProduct.getId(), 3);
        CartDto cart = cartService.addToCart(testUser.getId(), request2);
        
        // Then
        assertNotNull(cart);
        assertEquals(1, cart.getTotalItems());
        assertEquals(5, cart.getTotalQuantity()); // 2 + 3 = 5
        assertEquals(new BigDecimal("50.00"), cart.getTotalAmount());
        
        // Verify cart item quantity is updated
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }
    
    @Test
    void testUpdateCartItem() {
        // Given - Add item to cart first
        AddToCartRequest addRequest = new AddToCartRequest(testProduct.getId(), 2);
        cartService.addToCart(testUser.getId(), addRequest);
        
        // When - Update quantity
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(testProduct.getId(), 5);
        CartDto cart = cartService.updateCartItem(testUser.getId(), updateRequest);
        
        // Then
        assertNotNull(cart);
        assertEquals(1, cart.getTotalItems());
        assertEquals(5, cart.getTotalQuantity());
        assertEquals(new BigDecimal("50.00"), cart.getTotalAmount());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }
    
    @Test
    void testRemoveFromCart() {
        // Given - Add item to cart first
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 2);
        cartService.addToCart(testUser.getId(), request);
        
        // When - Remove item
        CartDto cart = cartService.removeFromCart(testUser.getId(), testProduct.getId());
        
        // Then
        assertNotNull(cart);
        assertEquals(0, cart.getTotalItems());
        assertEquals(0, cart.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertTrue(cart.getItems().isEmpty());
    }
    
    @Test
    void testGetCart_EmptyCart() {
        // When
        CartDto cart = cartService.getCart(testUser.getId());
        
        // Then
        assertNotNull(cart);
        assertEquals(testUser.getId(), cart.getUserId());
        assertEquals(0, cart.getTotalItems());
        assertEquals(0, cart.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertTrue(cart.getItems().isEmpty());
    }
    
    @Test
    void testClearCart() {
        // Given - Add items to cart
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 2);
        cartService.addToCart(testUser.getId(), request);
        
        // When - Clear cart
        cartService.clearCart(testUser.getId());
        
        // Then - Verify cart is empty
        CartDto cart = cartService.getCart(testUser.getId());
        assertEquals(0, cart.getTotalItems());
        assertEquals(0, cart.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertTrue(cart.getItems().isEmpty());
    }
    
    @Test
    void testAddToCart_InsufficientStock() {
        // Given - Product with limited stock
        testProduct.setStockQuantity(5);
        productRepository.save(testProduct);
        
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 10); // More than available
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(testUser.getId(), request);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }
    
    @Test
    void testAddToCart_InactiveProduct() {
        // Given - Inactive product
        testProduct.setIsActive(false);
        productRepository.save(testProduct);
        
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 2);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(testUser.getId(), request);
        });
        
        assertTrue(exception.getMessage().contains("Product is not available"));
    }
    
    @Test
    void testGetCartItemCount() {
        // Given - Add items to cart
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 3);
        cartService.addToCart(testUser.getId(), request);
        
        // When
        Long count = cartService.getCartItemCount(testUser.getId());
        
        // Then
        assertEquals(1L, count); // 1 unique item
    }
    
    @Test
    void testGetCartTotal() {
        // Given - Add items to cart
        AddToCartRequest request = new AddToCartRequest(testProduct.getId(), 3);
        cartService.addToCart(testUser.getId(), request);
        
        // When
        BigDecimal total = cartService.getCartTotal(testUser.getId());
        
        // Then
        assertEquals(new BigDecimal("30.00"), total);
    }
}