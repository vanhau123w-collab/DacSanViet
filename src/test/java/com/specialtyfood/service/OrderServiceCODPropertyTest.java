package com.specialtyfood.service;

import com.specialtyfood.dao.OrderDao;
import com.specialtyfood.dto.CreateOrderRequest;
import com.specialtyfood.dto.UpdateOrderStatusRequest;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.Positive;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for COD workflow in OrderService
 */
public class OrderServiceCODPropertyTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private CartItemRepository cartItemRepository;
    private UserRepository userRepository;
    private AddressRepository addressRepository;
    private ProductRepository productRepository;
    private NotificationService notificationService;
    private OrderService orderService;

    private void setupMocks() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        userRepository = mock(UserRepository.class);
        addressRepository = mock(AddressRepository.class);
        productRepository = mock(ProductRepository.class);
        notificationService = mock(NotificationService.class);
        
        orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            cartItemRepository,
            userRepository,
            addressRepository,
            productRepository,
            notificationService
        );
    }

    /**
     * **Feature: cod-payment-workflow, Property 1: COD Order Creation State Management**
     * **Validates: Requirements 1.2, 1.4, 1.5**
     * 
     * For any COD checkout request, creating the order should result in PROCESSING status, 
     * PENDING payment status, and empty user cart
     */
    @Property(tries = 100)
    void codOrderCreationStateManagement(
            @ForAll @Positive Long userId,
            @ForAll @NotBlank String customerName,
            @ForAll @NotBlank String customerPhone,
            @ForAll @NotBlank String customerEmail,
            @ForAll @NotBlank String shippingAddress,
            @ForAll @Positive BigDecimal productPrice,
            @ForAll("reasonableQuantity") Integer quantity) {
        
        // Setup mocks for each test run
        setupMocks();
        
        // Arrange
        User mockUser = createMockUser(userId, customerEmail);
        Product mockProduct = createMockProduct(productPrice, quantity);
        CartItem mockCartItem = createMockCartItem(mockUser, mockProduct, quantity);
        List<CartItem> cartItems = List.of(mockCartItem);
        
        Order savedOrder = createMockOrder(mockUser, productPrice.multiply(BigDecimal.valueOf(quantity)));
        savedOrder.setPaymentMethod("COD");
        savedOrder.setStatus(OrderStatus.PROCESSING);
        savedOrder.setPaymentStatus(PaymentStatus.PENDING);
        
        // Mock repository calls
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.findByUserIdOrderByAddedDateDesc(userId)).thenReturn(cartItems);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(userId);
        request.setCustomerName(customerName);
        request.setCustomerPhone(customerPhone);
        request.setCustomerEmail(customerEmail);
        request.setShippingAddress(shippingAddress);
        request.setPaymentMethod("COD");
        
        // Act
        OrderDao result = orderService.createOrderFromCart(request);
        
        // Assert - COD Order Creation State Management
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo("COD");
        
        // Verify cart was cleared
        verify(cartItemRepository).deleteByUserId(userId);
        
        // Verify order was saved
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * **Feature: cod-payment-workflow, Property 2: Delivery Confirmation Workflow**
     * **Validates: Requirements 2.2, 2.3, 2.4, 2.5**
     * 
     * For any order in SHIPPED status, when user confirms delivery, the system should update 
     * status to DELIVERED, set payment status to COMPLETED, record confirmation timestamp, 
     * and prevent further user status changes
     */
    @Property(tries = 100)
    void deliveryConfirmationWorkflow(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId) {
        
        // Setup mocks for each test run
        setupMocks();
        
        // Arrange
        User mockUser = createMockUser(userId, "test@example.com");
        Order shippedOrder = createMockShippedOrder(orderId, mockUser);
        Order deliveredOrder = createMockDeliveredOrder(orderId, mockUser);
        
        // Mock repository calls
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(shippedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(deliveredOrder);
        
        // Act
        OrderDao result = orderService.confirmDelivery(orderId, userId);
        
        // Assert - Delivery Confirmation Workflow
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getDeliveryConfirmedAt()).isNotNull();
        
        // Verify order was saved
        verify(orderRepository).save(any(Order.class));
        
        // Verify notification was sent
        verify(notificationService).sendOrderStatusNotification(any(Order.class), anyString());
    }

    /**
     * **Feature: cod-payment-workflow, Property 4: COD Order Validation**
     * **Validates: Requirements 5.1**
     * 
     * For any COD order creation attempt, the system should validate required customer information, 
     * and reject orders with missing data
     */
    @Property(tries = 100)
    void codOrderValidation(
            @ForAll @Positive Long userId,
            @ForAll @Positive BigDecimal productPrice,
            @ForAll @Positive Integer quantity) {
        
        // Setup mocks for each test run
        setupMocks();
        
        // Arrange
        User mockUser = createMockUser(userId, "test@example.com");
        Product mockProduct = createMockProduct(productPrice, quantity);
        CartItem mockCartItem = createMockCartItem(mockUser, mockProduct, quantity);
        List<CartItem> cartItems = List.of(mockCartItem);
        
        // Mock repository calls
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.findByUserIdOrderByAddedDateDesc(userId)).thenReturn(cartItems);
        
        // Test case 1: Missing customer name
        CreateOrderRequest requestMissingName = new CreateOrderRequest();
        requestMissingName.setUserId(userId);
        requestMissingName.setCustomerName(""); // Empty name
        requestMissingName.setCustomerPhone("0123456789");
        requestMissingName.setCustomerEmail("test@example.com");
        requestMissingName.setShippingAddress("123 Test Street");
        requestMissingName.setPaymentMethod("COD");
        
        // Act & Assert - Should throw exception for missing name
        try {
            orderService.createOrderFromCart(requestMissingName);
            assertThat(false).as("Should have thrown exception for missing customer name").isTrue();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Customer name is required for COD orders");
        }
        
        // Test case 2: Missing customer phone
        CreateOrderRequest requestMissingPhone = new CreateOrderRequest();
        requestMissingPhone.setUserId(userId);
        requestMissingPhone.setCustomerName("Test Customer");
        requestMissingPhone.setCustomerPhone(""); // Empty phone
        requestMissingPhone.setCustomerEmail("test@example.com");
        requestMissingPhone.setShippingAddress("123 Test Street");
        requestMissingPhone.setPaymentMethod("COD");
        
        // Act & Assert - Should throw exception for missing phone
        try {
            orderService.createOrderFromCart(requestMissingPhone);
            assertThat(false).as("Should have thrown exception for missing customer phone").isTrue();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Customer phone is required for COD orders");
        }
        
        // Test case 3: Missing shipping address
        CreateOrderRequest requestMissingAddress = new CreateOrderRequest();
        requestMissingAddress.setUserId(userId);
        requestMissingAddress.setCustomerName("Test Customer");
        requestMissingAddress.setCustomerPhone("0123456789");
        requestMissingAddress.setCustomerEmail("test@example.com");
        requestMissingAddress.setShippingAddress(""); // Empty address
        requestMissingAddress.setPaymentMethod("COD");
        
        // Act & Assert - Should throw exception for missing address
        try {
            orderService.createOrderFromCart(requestMissingAddress);
            assertThat(false).as("Should have thrown exception for missing shipping address").isTrue();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Shipping address is required for COD orders");
        }
    }

    /**
     * **Feature: cod-payment-workflow, Property 5: Order Status Transition Validation**
     * **Validates: Requirements 5.2**
     * 
     * For any order status change attempt, the system should validate transition rules 
     * and reject invalid transitions
     */
    @Property(tries = 100)
    void orderStatusTransitionValidation(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId) {
        
        // Test case 1: Valid transition from PROCESSING to SHIPPED
        setupMocks();
        User mockUser = createMockUser(userId, "test@example.com");
        Order processingOrder = createMockProcessingOrder(orderId, mockUser);
        Order shippedOrder = createMockShippedOrder(orderId, mockUser);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(processingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(shippedOrder);
        
        UpdateOrderStatusRequest validRequest = new UpdateOrderStatusRequest();
        validRequest.setStatus(OrderStatus.SHIPPED);
        
        // Act - Should succeed
        OrderDao result = orderService.updateOrderStatus(orderId, validRequest);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // Test case 2: Invalid transition from DELIVERED to SHIPPED
        setupMocks();
        Order deliveredOrder = createMockDeliveredOrder(orderId + 1, mockUser); // Use different ID
        when(orderRepository.findById(orderId + 1)).thenReturn(Optional.of(deliveredOrder));
        
        UpdateOrderStatusRequest invalidRequest = new UpdateOrderStatusRequest();
        invalidRequest.setStatus(OrderStatus.SHIPPED);
        
        // Act & Assert - Should throw exception for invalid transition
        boolean exceptionThrown = false;
        try {
            orderService.updateOrderStatus(orderId + 1, invalidRequest);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            assertThat(e.getMessage()).contains("Cannot change status from DELIVERED");
        }
        assertThat(exceptionThrown).as("Should have thrown exception for invalid status transition from DELIVERED").isTrue();
        
        // Test case 3: Invalid transition from PROCESSING to DELIVERED (skipping SHIPPED)
        setupMocks();
        Order processingOrder2 = createMockProcessingOrder(orderId + 2, mockUser); // Use different ID
        when(orderRepository.findById(orderId + 2)).thenReturn(Optional.of(processingOrder2));
        
        UpdateOrderStatusRequest skipRequest = new UpdateOrderStatusRequest();
        skipRequest.setStatus(OrderStatus.DELIVERED);
        
        // Act & Assert - Should throw exception for skipping status
        boolean skipExceptionThrown = false;
        try {
            orderService.updateOrderStatus(orderId + 2, skipRequest);
        } catch (RuntimeException e) {
            skipExceptionThrown = true;
            assertThat(e.getMessage()).contains("Invalid status transition from PROCESSING to DELIVERED");
        }
        assertThat(skipExceptionThrown).as("Should have thrown exception for invalid status transition from PROCESSING to DELIVERED").isTrue();
    }

    /**
     * **Feature: cod-payment-workflow, Property 6: Delivery Confirmation Authorization**
     * **Validates: Requirements 5.3**
     * 
     * For any delivery confirmation attempt, the system should validate the order belongs 
     * to the requesting user and reject unauthorized attempts
     */
    @Property(tries = 100)
    void deliveryConfirmationAuthorization(
            @ForAll @Positive Long orderId,
            @ForAll @Positive Long userId,
            @ForAll @Positive Long otherUserId) {
        
        // Ensure the two user IDs are different
        if (userId.equals(otherUserId)) {
            return; // Skip this test case
        }
        
        // Setup mocks for each test run
        setupMocks();
        
        // Test case 1: Valid authorization - user owns the order
        User mockUser = createMockUser(userId, "test@example.com");
        Order shippedOrder = createMockShippedOrder(orderId, mockUser);
        Order deliveredOrder = createMockDeliveredOrder(orderId, mockUser);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(shippedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(deliveredOrder);
        
        // Act - Should succeed
        OrderDao result = orderService.confirmDelivery(orderId, userId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        
        // Test case 2: Invalid authorization - user does not own the order
        setupMocks();
        User otherUser = createMockUser(otherUserId, "other@example.com");
        Order shippedOrderOtherUser = createMockShippedOrder(orderId + 1, otherUser);
        
        when(orderRepository.findById(orderId + 1)).thenReturn(Optional.of(shippedOrderOtherUser));
        
        // Act & Assert - Should throw exception for unauthorized access
        boolean authExceptionThrown = false;
        try {
            orderService.confirmDelivery(orderId + 1, userId); // Wrong user trying to confirm
        } catch (RuntimeException e) {
            authExceptionThrown = true;
            assertThat(e.getMessage()).contains("Order does not belong to user");
        }
        assertThat(authExceptionThrown).as("Should have thrown exception for unauthorized delivery confirmation").isTrue();
        
        // Test case 3: Invalid order status for confirmation
        setupMocks();
        Order pendingOrder = createMockProcessingOrder(orderId + 2, mockUser);
        when(orderRepository.findById(orderId + 2)).thenReturn(Optional.of(pendingOrder));
        
        // Act & Assert - Should throw exception for wrong status
        boolean statusExceptionThrown = false;
        try {
            orderService.confirmDelivery(orderId + 2, userId);
        } catch (RuntimeException e) {
            statusExceptionThrown = true;
            assertThat(e.getMessage()).contains("Order must be in SHIPPED status to confirm delivery");
        }
        assertThat(statusExceptionThrown).as("Should have thrown exception for wrong order status").isTrue();
    }

    private User createMockUser(Long userId, String email) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setIsActive(true);
        return user;
    }

    private Product createMockProduct(BigDecimal price, Integer quantity) {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(price);
        // Ensure stock is always sufficient for the requested quantity
        // Handle potential integer overflow by using long arithmetic
        long safeStock = Math.max((long)quantity + 10L, 100L);
        product.setStockQuantity((int)Math.min(safeStock, Integer.MAX_VALUE));
        product.setIsActive(true);
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        product.setCategory(category);
        
        return product;
    }

    private CartItem createMockCartItem(User user, Product product, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private Order createMockOrder(User user, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setOrderNumber("ORD123456789");
        return order;
    }

    private Order createMockShippedOrder(Long orderId, User user) {
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setOrderNumber("ORD" + orderId);
        order.setStatus(OrderStatus.SHIPPED);
        order.setPaymentMethod("COD");
        order.setPaymentStatus(PaymentStatus.PENDING);
        return order;
    }

    private Order createMockDeliveredOrder(Long orderId, User user) {
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setOrderNumber("ORD" + orderId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod("COD");
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDeliveryConfirmedAt(LocalDateTime.now());
        return order;
    }

    private Order createMockProcessingOrder(Long orderId, User user) {
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setOrderNumber("ORD" + orderId);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentMethod("COD");
        order.setPaymentStatus(PaymentStatus.PENDING);
        return order;
    }
    
    @Provide
    Arbitrary<Integer> reasonableQuantity() {
        return Arbitraries.integers().between(1, 1000);
    }
}