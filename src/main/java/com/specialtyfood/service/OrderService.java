package com.specialtyfood.service;

import com.specialtyfood.dao.*;
import com.specialtyfood.dto.CreateOrderRequest;
import com.specialtyfood.dto.UpdateOrderStatusRequest;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for order operations
 */
@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       OrderItemRepository orderItemRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       AddressRepository addressRepository,
                       ProductRepository productRepository,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Create order from user's cart (OLD VERSION - DEPRECATED)
     */
    /*
    public OrderDao createOrder(Long userId, CreateOrderRequest request) {
        User user = getUserById(userId);
        Address shippingAddress = getAddressById(request.getShippingAddressId());
        
        // Validate that address belongs to user
        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }
        
        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedDateDesc(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Validate cart items and check inventory
        validateCartForOrder(cartItems);
        
        // Calculate totals
        BigDecimal subtotal = calculateCartSubtotal(cartItems);
        BigDecimal shippingFee = calculateShippingFee(subtotal);
        BigDecimal taxAmount = calculateTaxAmount(subtotal);
        BigDecimal totalAmount = subtotal.add(shippingFee).add(taxAmount);
        
        // Create order
        Order order = new Order(user, totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);
        
        // Save order
        order = orderRepository.save(order);
        
        // Create order items from cart items and update inventory
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(order, cartItem);
            orderItemRepository.save(orderItem);
            order.addOrderItem(orderItem);
            
            // Update product stock directly
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            
            // Check for low stock and notify
            checkLowStockAndNotify(product);
        }
        
        // Clear cart after successful order creation
        cartItemRepository.deleteByUserId(userId);
        
        // Send payment confirmation notification
        notificationService.sendPaymentConfirmation(order);
        
        return convertToOrderDto(order);
    }
    */
    
    /**
     * Update order status
     */
    public OrderDao updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());
        
        order.setStatus(request.getStatus());
        
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        
        order = orderRepository.save(order);
        
        // Send notification about status change
        String statusMessage = getStatusChangeMessage(request.getStatus());
        notificationService.sendOrderStatusNotification(order, statusMessage);
        
        return convertToOrderDto(order);
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDao getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToOrderDto(order);
    }
    
    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderDao getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        return convertToOrderDto(order);
    }
    
    /**
     * Get orders by user
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getOrdersByUser(Long userId, Pageable pageable) {
        getUserById(userId); // Validate user exists
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Get orders by user and status
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getOrdersByUserAndStatus(Long userId, OrderStatus status, Pageable pageable) {
        getUserById(userId); // Validate user exists
        Page<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Get all orders (admin)
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Get orders by status (admin)
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Search orders (admin)
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> searchOrders(String searchTerm, Pageable pageable) {
        Page<Order> orders = orderRepository.searchOrders(searchTerm, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Cancel order
     */
    public OrderDao cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Validate user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }
        
        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        // Restore inventory
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        
        // Send notification about cancellation
        notificationService.sendOrderStatusNotification(order, "Your order has been cancelled and inventory has been restored.");
        
        return convertToOrderDto(order);
    }
    
    /**
     * Get order statistics
     */
    @Transactional(readOnly = true)
    public OrderStatisticsDao getOrderStatistics() {
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        Long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        Long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        
        OrderStatisticsDao stats = new OrderStatisticsDao();
        stats.setTotalOrders(totalOrders);
        stats.setPendingOrders(pendingOrders);
        stats.setConfirmedOrders(confirmedOrders);
        stats.setShippedOrders(shippedOrders);
        stats.setDeliveredOrders(deliveredOrders);
        stats.setCancelledOrders(cancelledOrders);
        stats.setTotalRevenue(totalRevenue);
        
        return stats;
    }
    
    /**
     * Confirm delivery for COD orders
     */
    public OrderDao confirmDelivery(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Validate user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }
        
        // Validate order status
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order must be in SHIPPED status to confirm delivery. Current status: " + order.getStatus());
        }
        
        // Validate payment method is COD
        if (!"COD".equals(order.getPaymentMethod())) {
            throw new RuntimeException("Delivery confirmation is only available for COD orders");
        }
        
        // Update order status and payment status
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDeliveryConfirmedAt(LocalDateTime.now());
        
        order = orderRepository.save(order);
        
        // Send notification about delivery confirmation
        notificationService.sendOrderStatusNotification(order, "Cảm ơn bạn đã xác nhận nhận hàng. Đơn hàng đã được hoàn tất.");
        
        return convertToOrderDto(order);
    }
    
    /**
     * Validate COD order requirements
     */
    private void validateCODOrder(CreateOrderRequest request) {
        if ("COD".equals(request.getPaymentMethod())) {
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                throw new RuntimeException("Customer name is required for COD orders");
            }
            if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
                throw new RuntimeException("Customer phone is required for COD orders");
            }
            if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
                throw new RuntimeException("Shipping address is required for COD orders");
            }
        }
    }

    // Helper methods
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    
    private Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
    }
    

    
    private void validateCartForOrder(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Check if product is active
            if (!product.getIsActive()) {
                throw new RuntimeException("Product is no longer available: " + product.getName());
            }
            
            // Check if sufficient stock is available
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + 
                                         ". Available: " + product.getStockQuantity() + 
                                         ", Requested: " + cartItem.getQuantity());
            }
        }
    }
    
    private BigDecimal calculateCartSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        // Simple shipping calculation - free shipping over 500,000 VND
        BigDecimal freeShippingThreshold = new BigDecimal("500000");
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("30000"); // 30,000 VND shipping fee
    }
    
    private BigDecimal calculateTaxAmount(BigDecimal subtotal) {
        // 10% VAT
        return subtotal.multiply(new BigDecimal("0.10"));
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from PROCESSING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException("Invalid status transition from SHIPPED to " + newStatus);
                }
                break;
            case DELIVERED:
            case CANCELLED:
                throw new RuntimeException("Cannot change status from " + currentStatus);
            default:
                throw new RuntimeException("Unknown order status: " + currentStatus);
        }
    }
    
    private OrderDao convertToOrderDto(Order order) {
        OrderDao dto = new OrderDao();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        dto.setUserFullName(order.getUser().getFullName());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setShippedDate(order.getShippedDate());
        dto.setDeliveredDate(order.getDeliveredDate());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setDeliveryConfirmedAt(order.getDeliveryConfirmedAt());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setShippingAddressText(order.getShippingAddressText());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Convert shipping address
        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(convertToAddressDto(order.getShippingAddress()));
        }
        
        // Convert order items
        List<OrderItemDao> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);
        
        return dto;
    }
    
    private OrderItemDao convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDao dto = new OrderItemDao();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProductName());
        dto.setProductDescription(orderItem.getProductDescription());
        dto.setCategoryName(orderItem.getCategoryName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setCreatedAt(orderItem.getCreatedAt());
        return dto;
    }
    
    private AddressDao convertToAddressDto(Address address) {
        AddressDao dto = new AddressDao();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setPhoneNumber(address.getPhoneNumber());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setProvince(address.getProvince());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setIsDefault(address.getIsDefault());
        dto.setUserId(address.getUser().getId());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        return dto;
    }
    
    private String getStatusChangeMessage(OrderStatus status) {
        switch (status) {
            case CONFIRMED:
                return "Your order has been confirmed and is being prepared for shipment.";
            case SHIPPED:
                return "Your order has been shipped and is on its way to you.";
            case DELIVERED:
                return "Your order has been delivered successfully. Thank you for your purchase!";
            case CANCELLED:
                return "Your order has been cancelled.";
            default:
                return "Your order status has been updated.";
        }
    }
    
    private void checkLowStockAndNotify(Product product) {
        // Low stock threshold
        final int LOW_STOCK_THRESHOLD = 10;
        
        if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD && product.getIsActive()) {
            notificationService.sendLowStockNotification(product);
        }
        
        if (product.getStockQuantity() == 0) {
            notificationService.sendOutOfStockNotification(product);
        }
    }
    
    // ===== ADMIN ANALYTICS METHODS =====
    
    /**
     * Get orders by status and date range
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersByStatusAndDateRange(status, startDate, endDate, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Get orders by date range
     */
    @Transactional(readOnly = true)
    public Page<OrderDao> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersBetweenDates(startDate, endDate, pageable);
        return orders.map(this::convertToOrderDto);
    }
    
    /**
     * Count orders by status
     */
    @Transactional(readOnly = true)
    public Long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
    
    /**
     * Get total amount spent by user
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpentByUser(Long userId) {
        return orderRepository.calculateTotalSpentByUser(userId);
    }
    
    /**
     * Get average order value by user
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageOrderValueByUser(Long userId) {
        return orderRepository.calculateAverageOrderValueByUser(userId);
    }
    
    /**
     * Get revenue analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate, String period) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total revenue for period
        BigDecimal totalRevenue = orderRepository.calculateRevenueBetweenDates(startDate, endDate);
        analytics.put("totalRevenue", totalRevenue);
        
        // Revenue breakdown by period
        List<Object[]> revenueBreakdown;
        if ("DAILY".equalsIgnoreCase(period)) {
            revenueBreakdown = orderRepository.getDailyOrderStatistics(startDate, endDate);
        } else {
            revenueBreakdown = orderRepository.getMonthlyOrderStatistics();
        }
        analytics.put("revenueBreakdown", revenueBreakdown);
        
        // Growth rate calculation
        LocalDateTime previousPeriodStart = startDate.minusDays(java.time.Duration.between(startDate, endDate).toDays());
        BigDecimal previousRevenue = orderRepository.calculateRevenueBetweenDates(previousPeriodStart, startDate);
        
        BigDecimal growthRate = BigDecimal.ZERO;
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = totalRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        analytics.put("growthRate", growthRate);
        
        return analytics;
    }
    
    /**
     * Get popular products
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getPopularProducts(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Object[]> popularProductsData = orderRepository.findPopularProducts(startDate, endDate, pageable);
        return popularProductsData.map(data -> {
            Product product = (Product) data[0];
            Long totalSold = (Long) data[1];
            BigDecimal totalRevenue = (BigDecimal) data[2];
            
            ProductDao dto = convertToProductDto(product);
            dto.setTotalSold(totalSold);
            dto.setTotalRevenue(totalRevenue);
            return dto;
        });
    }
    
    /**
     * Get customer trends
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerTrends(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> trends = new HashMap<>();
        
        // New customers in period
        Long newCustomers = userRepository.countNewCustomers(startDate, endDate);
        trends.put("newCustomers", newCustomers);
        
        // Returning customers
        Long returningCustomers = orderRepository.countReturningCustomers(startDate, endDate);
        trends.put("returningCustomers", returningCustomers);
        
        // Average order frequency
        Double avgOrderFrequency = orderRepository.calculateAverageOrderFrequency(startDate, endDate);
        trends.put("averageOrderFrequency", avgOrderFrequency);
        
        return trends;
    }
    
    /**
     * Get order status breakdown
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStatusBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Long> breakdown = new HashMap<>();
        
        for (OrderStatus status : OrderStatus.values()) {
            Long count = orderRepository.countByStatusAndDateRange(status, startDate, endDate);
            breakdown.put(status.name(), count);
        }
        
        return breakdown;
    }
    
    /**
     * Get top customers by spending
     */
    @Transactional(readOnly = true)
    public Page<UserDao> getTopCustomersBySpending(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Object[]> topCustomersData = orderRepository.findTopCustomersBySpendingInPeriod(startDate, endDate, pageable);
        return topCustomersData.map(data -> {
            User user = (User) data[0];
            Long orderCount = (Long) data[1];
            BigDecimal totalSpent = (BigDecimal) data[2];
            
            UserDao dto = convertToUserDto(user);
            dto.setOrderCount(orderCount);
            dto.setTotalSpent(totalSpent);
            return dto;
        });
    }
    
    /**
     * Get category performance
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> categoryData = orderRepository.getCategoryPerformance(startDate, endDate);
        
        Map<String, Object> performance = new HashMap<>();
        List<Map<String, Object>> categories = new ArrayList<>();
        
        for (Object[] data : categoryData) {
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("categoryName", data[0]);
            categoryInfo.put("totalSold", data[1]);
            categoryInfo.put("totalRevenue", data[2]);
            categories.add(categoryInfo);
        }
        
        performance.put("categories", categories);
        return performance;
    }
    
    /**
     * Get low performing products
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getLowPerformingProducts(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Object[]> lowPerformingData = orderRepository.findLowPerformingProducts(startDate, endDate, pageable);
        return lowPerformingData.map(data -> {
            Product product = (Product) data[0];
            Long totalSold = (Long) data[1];
            
            ProductDao dto = convertToProductDto(product);
            dto.setTotalSold(totalSold);
            return dto;
        });
    }
    
    /**
     * Get customer segmentation
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerSegmentation(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> segmentation = new HashMap<>();
        
        // New vs returning customers
        Long newCustomers = userRepository.countNewCustomers(startDate, endDate);
        Long returningCustomers = orderRepository.countReturningCustomers(startDate, endDate);
        
        segmentation.put("newCustomers", newCustomers);
        segmentation.put("returningCustomers", returningCustomers);
        
        // Customer lifetime value segments
        List<Object[]> clvSegments = orderRepository.getCustomerLifetimeValueSegments();
        segmentation.put("lifetimeValueSegments", clvSegments);
        
        return segmentation;
    }
    
    /**
     * Get customer retention metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerRetentionMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Repeat purchase rate
        Double repeatPurchaseRate = orderRepository.calculateRepeatPurchaseRate(startDate, endDate);
        metrics.put("repeatPurchaseRate", repeatPurchaseRate);
        
        // Customer churn rate
        Double churnRate = orderRepository.calculateCustomerChurnRate(startDate, endDate);
        metrics.put("churnRate", churnRate);
        
        // Average customer lifespan
        Double avgCustomerLifespan = orderRepository.calculateAverageCustomerLifespan();
        metrics.put("averageCustomerLifespan", avgCustomerLifespan);
        
        return metrics;
    }
    
    // Helper conversion methods
    
    private ProductDao convertToProductDto(Product product) {
        ProductDao dto = new ProductDao();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
    
    private UserDao convertToUserDto(User user) {
        return new UserDao(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAdmin(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    /**
     * Create order from cart with customer information
     */
    public OrderDao createOrderFromCart(CreateOrderRequest request) {
        User user = getUserById(request.getUserId());
        
        // Debug logging
        System.out.println("Creating order with payment method: " + request.getPaymentMethod());
        
        // Validate COD order requirements
        validateCODOrder(request);
        
        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedDateDesc(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Validate cart items and check inventory
        validateCartForOrder(cartItems);
        
        // Calculate totals
        BigDecimal subtotal = calculateCartSubtotal(cartItems);
        BigDecimal shippingFee = BigDecimal.ZERO; // Free shipping for now
        BigDecimal taxAmount = BigDecimal.ZERO; // No tax for now
        BigDecimal totalAmount = subtotal.add(shippingFee).add(taxAmount);
        
        // Create order
        Order order = new Order(user, totalAmount);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddressText(request.getShippingAddress());
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        
        // Set appropriate status based on payment method
        if ("COD".equals(request.getPaymentMethod())) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setPaymentStatus(PaymentStatus.PENDING);
            System.out.println("COD order created with PROCESSING status");
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);
            System.out.println("Non-COD order created with PENDING status");
        }
        
        // Generate order number
        order.setOrderNumber(generateOrderNumber());
        
        // Save order
        order = orderRepository.save(order);
        
        // Create order items from cart items and update inventory
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(order, cartItem);
            orderItemRepository.save(orderItem);
            order.addOrderItem(orderItem);
            
            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // Clear cart
        cartItemRepository.deleteByUserId(request.getUserId());
        
        // Send notification
        try {
            // notificationService.sendOrderConfirmation(order); // TODO: Implement this method
            System.out.println("Order created successfully: " + order.getOrderNumber());
        } catch (Exception e) {
            // Log error but don't fail the order
            System.err.println("Failed to send order confirmation: " + e.getMessage());
        }
        
        return convertToOrderDto(order);
    }
    
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }
}