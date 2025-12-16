package com.specialtyfood.service;

import com.specialtyfood.dto.*;
import com.specialtyfood.model.*;
import com.specialtyfood.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin service for administrative operations and business logic
 * Handles order status updates with notifications, customer management operations,
 * and analytics and reporting services
 */
@Service
@Transactional
public class AdminService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final NotificationService notificationService;
    
    @Autowired
    public AdminService(OrderRepository orderRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       CategoryRepository categoryRepository,
                       OrderService orderService,
                       UserService userService,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderService = orderService;
        this.userService = userService;
        this.notificationService = notificationService;
    }
    
    // ===== ORDER STATUS UPDATE WITH NOTIFICATIONS =====
    
    /**
     * Update order status with comprehensive notifications
     * Requirements: 5.2 - Order status update with notifications
     */
    public OrderDto updateOrderStatusWithNotifications(Long orderId, UpdateOrderStatusRequest request, String adminUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        OrderStatus previousStatus = order.getStatus();
        
        // Validate status transition
        validateStatusTransition(previousStatus, request.getStatus());
        
        // Update order status
        order.setStatus(request.getStatus());
        
        // Update additional fields based on status
        updateOrderFieldsByStatus(order, request);
        
        // Save order
        order = orderRepository.save(order);
        
        // Send notifications to customer
        sendCustomerStatusNotification(order, previousStatus, adminUsername);
        
        // Send internal notifications if needed
        sendInternalStatusNotifications(order, previousStatus);
        
        // Log admin action
        logAdminAction(adminUsername, "ORDER_STATUS_UPDATE", 
                      String.format("Updated order %s from %s to %s", 
                                  order.getOrderNumber(), previousStatus, request.getStatus()));
        
        return convertToOrderDto(order);
    }
    
    /**
     * Bulk update order status for multiple orders
     * Requirements: 5.2 - Order management operations
     */
    public Map<String, Object> bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus, String adminUsername) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (Long orderId : orderIds) {
            try {
                UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
                request.setStatus(newStatus);
                updateOrderStatusWithNotifications(orderId, request, adminUsername);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add(String.format("Order %d: %s", orderId, e.getMessage()));
            }
        }
        
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        result.put("totalProcessed", orderIds.size());
        
        return result;
    }
    
    // ===== CUSTOMER MANAGEMENT OPERATIONS =====
    
    /**
     * Get comprehensive customer analytics
     * Requirements: 5.3 - Customer management operations
     */
    public Map<String, Object> getCustomerAnalytics(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic customer info
        UserDto customerDto = convertToUserDto(customer);
        analytics.put("customer", customerDto);
        
        // Order statistics
        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("totalOrders", orderRepository.countByUserId(customerId));
        orderStats.put("totalSpent", orderRepository.calculateTotalSpentByUser(customerId));
        orderStats.put("averageOrderValue", orderRepository.calculateAverageOrderValueByUser(customerId));
        
        // Order status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            Long count = orderRepository.countByUserIdAndStatus(customerId, status);
            statusBreakdown.put(status.name(), count);
        }
        orderStats.put("statusBreakdown", statusBreakdown);
        
        analytics.put("orderStatistics", orderStats);
        
        // Recent activity
        Pageable recentOrdersPageable = PageRequest.of(0, 5, Sort.by("orderDate").descending());
        Page<OrderDto> recentOrders = orderService.getOrdersByUser(customerId, recentOrdersPageable);
        analytics.put("recentOrders", recentOrders.getContent());
        
        // Customer behavior metrics
        Map<String, Object> behaviorMetrics = calculateCustomerBehaviorMetrics(customerId);
        analytics.put("behaviorMetrics", behaviorMetrics);
        
        return analytics;
    }
    
    /**
     * Manage customer account status with notifications
     * Requirements: 5.3 - Customer management operations
     */
    public UserDto manageCustomerStatus(Long customerId, boolean activate, String reason, String adminUsername) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        boolean previousStatus = customer.getIsActive();
        customer.setIsActive(activate);
        customer = userRepository.save(customer);
        
        // Send notification to customer
        String action = activate ? "activated" : "deactivated";
        String message = String.format("Your account has been %s. Reason: %s", action, reason);
        notificationService.sendAccountStatusNotification(customer, message);
        
        // Log admin action
        logAdminAction(adminUsername, "CUSTOMER_STATUS_UPDATE", 
                      String.format("Customer %s account %s. Reason: %s", 
                                  customer.getEmail(), action, reason));
        
        return convertToUserDto(customer);
    }
    
    /**
     * Search customers with advanced filters
     * Requirements: 5.3 - Search for specific orders and customers
     */
    public Page<UserDto> searchCustomersAdvanced(String searchTerm, Role role, Boolean isActive, 
                                               LocalDateTime registeredAfter, LocalDateTime registeredBefore,
                                               Pageable pageable) {
        Page<User> customers = userRepository.searchCustomersAdvanced(
            searchTerm, role, isActive, registeredAfter, registeredBefore, pageable);
        
        return customers.map(this::convertToUserDto);
    }
    
    // ===== ANALYTICS AND REPORTING SERVICES =====
    
    /**
     * Generate comprehensive business intelligence report
     * Requirements: 5.5 - Analytics and reporting services
     */
    public Map<String, Object> generateBusinessIntelligenceReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Executive summary
        Map<String, Object> executiveSummary = generateExecutiveSummary(startDate, endDate);
        report.put("executiveSummary", executiveSummary);
        
        // Sales performance
        Map<String, Object> salesPerformance = generateSalesPerformanceReport(startDate, endDate);
        report.put("salesPerformance", salesPerformance);
        
        // Customer insights
        Map<String, Object> customerInsights = generateCustomerInsightsReport(startDate, endDate);
        report.put("customerInsights", customerInsights);
        
        // Product performance
        Map<String, Object> productPerformance = generateProductPerformanceReport(startDate, endDate);
        report.put("productPerformance", productPerformance);
        
        // Operational metrics
        Map<String, Object> operationalMetrics = generateOperationalMetricsReport(startDate, endDate);
        report.put("operationalMetrics", operationalMetrics);
        
        // Report metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now());
        metadata.put("startDate", startDate);
        metadata.put("endDate", endDate);
        metadata.put("reportType", "BUSINESS_INTELLIGENCE");
        report.put("metadata", metadata);
        
        return report;
    }
    
    /**
     * Generate real-time dashboard metrics
     * Requirements: 5.1 - Order management dashboard
     */
    public Map<String, Object> generateDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Today's metrics
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        
        Map<String, Object> todayMetrics = new HashMap<>();
        todayMetrics.put("ordersToday", orderRepository.countOrdersBetweenDates(todayStart, now));
        todayMetrics.put("revenueToday", orderRepository.calculateRevenueBetweenDates(todayStart, now));
        todayMetrics.put("newCustomersToday", userRepository.countNewCustomers(todayStart, now));
        
        metrics.put("today", todayMetrics);
        
        // This week metrics
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Map<String, Object> weekMetrics = new HashMap<>();
        weekMetrics.put("ordersThisWeek", orderRepository.countOrdersBetweenDates(weekStart, now));
        weekMetrics.put("revenueThisWeek", orderRepository.calculateRevenueBetweenDates(weekStart, now));
        weekMetrics.put("newCustomersThisWeek", userRepository.countNewCustomers(weekStart, now));
        
        metrics.put("thisWeek", weekMetrics);
        
        // Pending actions
        Map<String, Object> pendingActions = new HashMap<>();
        pendingActions.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        pendingActions.put("ordersToShip", orderRepository.countByStatus(OrderStatus.CONFIRMED));
        pendingActions.put("lowStockProducts", productRepository.countLowStockProducts());
        
        metrics.put("pendingActions", pendingActions);
        
        // Performance indicators
        Map<String, Object> kpis = calculateKeyPerformanceIndicators();
        metrics.put("kpis", kpis);
        
        return metrics;
    }
    
    // ===== HELPER METHODS =====
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from PENDING to " + newStatus);
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
    
    private void updateOrderFieldsByStatus(Order order, UpdateOrderStatusRequest request) {
        switch (request.getStatus()) {
            case CONFIRMED:
                // No additional fields needed
                break;
            case SHIPPED:
                order.setShippedDate(LocalDateTime.now());
                if (request.getTrackingNumber() != null) {
                    order.setTrackingNumber(request.getTrackingNumber());
                }
                break;
            case DELIVERED:
                order.setDeliveredDate(LocalDateTime.now());
                break;
            case CANCELLED:
                // Restore inventory
                restoreInventoryForCancelledOrder(order);
                break;
        }
        
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
    }
    
    private void restoreInventoryForCancelledOrder(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }
    }
    
    private void sendCustomerStatusNotification(Order order, OrderStatus previousStatus, String adminUsername) {
        String message = generateStatusChangeMessage(order.getStatus(), previousStatus);
        notificationService.sendOrderStatusNotification(order, message);
        
        // Send email notification for important status changes
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            notificationService.sendOrderStatusEmail(order, message);
        }
    }
    
    private void sendInternalStatusNotifications(Order order, OrderStatus previousStatus) {
        // Notify relevant departments based on status change
        switch (order.getStatus()) {
            case CONFIRMED:
                notificationService.sendInternalNotification("WAREHOUSE", 
                    String.format("Order %s confirmed and ready for fulfillment", order.getOrderNumber()));
                break;
            case SHIPPED:
                notificationService.sendInternalNotification("CUSTOMER_SERVICE", 
                    String.format("Order %s shipped with tracking: %s", order.getOrderNumber(), order.getTrackingNumber()));
                break;
            case DELIVERED:
                notificationService.sendInternalNotification("SALES", 
                    String.format("Order %s delivered successfully", order.getOrderNumber()));
                break;
        }
    }
    
    private String generateStatusChangeMessage(OrderStatus newStatus, OrderStatus previousStatus) {
        switch (newStatus) {
            case CONFIRMED:
                return "Your order has been confirmed and is being prepared for shipment.";
            case SHIPPED:
                return "Your order has been shipped and is on its way to you.";
            case DELIVERED:
                return "Your order has been delivered successfully. Thank you for your purchase!";
            case CANCELLED:
                return "Your order has been cancelled. If you have any questions, please contact customer service.";
            default:
                return "Your order status has been updated.";
        }
    }
    
    private void logAdminAction(String adminUsername, String actionType, String description) {
        // Log admin actions for audit trail
        // This could be implemented with a separate AdminActionLog entity
        System.out.println(String.format("[ADMIN_ACTION] %s - %s: %s", 
                                       LocalDateTime.now(), adminUsername, description));
    }
    
    private Map<String, Object> calculateCustomerBehaviorMetrics(Long customerId) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate metrics like order frequency, seasonal patterns, etc.
        List<Order> customerOrders = orderRepository.findByUserIdOrderByOrderDateDesc(customerId, Pageable.unpaged()).getContent();
        
        if (!customerOrders.isEmpty()) {
            // Average days between orders
            if (customerOrders.size() > 1) {
                long totalDays = 0;
                for (int i = 0; i < customerOrders.size() - 1; i++) {
                    totalDays += java.time.Duration.between(
                        customerOrders.get(i + 1).getOrderDate(),
                        customerOrders.get(i).getOrderDate()
                    ).toDays();
                }
                metrics.put("averageDaysBetweenOrders", totalDays / (customerOrders.size() - 1));
            }
            
            // Favorite categories
            Map<String, Long> categoryFrequency = customerOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getCategoryName(),
                    Collectors.counting()
                ));
            metrics.put("favoriteCategories", categoryFrequency);
            
            // Last order date
            metrics.put("lastOrderDate", customerOrders.get(0).getOrderDate());
            
            // Customer lifetime (days since first order)
            LocalDateTime firstOrderDate = customerOrders.get(customerOrders.size() - 1).getOrderDate();
            long customerLifetimeDays = java.time.Duration.between(firstOrderDate, LocalDateTime.now()).toDays();
            metrics.put("customerLifetimeDays", customerLifetimeDays);
        }
        
        return metrics;
    }
    
    private Map<String, Object> generateExecutiveSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        // Key metrics
        summary.put("totalRevenue", orderRepository.calculateRevenueBetweenDates(startDate, endDate));
        summary.put("totalOrders", orderRepository.countOrdersBetweenDates(startDate, endDate));
        summary.put("newCustomers", userRepository.countNewCustomers(startDate, endDate));
        summary.put("averageOrderValue", orderRepository.calculateAverageOrderValueInPeriod(startDate, endDate));
        
        // Growth rates (compared to previous period)
        long periodDays = java.time.Duration.between(startDate, endDate).toDays();
        LocalDateTime previousPeriodStart = startDate.minusDays(periodDays);
        
        BigDecimal currentRevenue = orderRepository.calculateRevenueBetweenDates(startDate, endDate);
        BigDecimal previousRevenue = orderRepository.calculateRevenueBetweenDates(previousPeriodStart, startDate);
        
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal revenueGrowth = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.put("revenueGrowthRate", revenueGrowth);
        }
        
        return summary;
    }
    
    private Map<String, Object> generateSalesPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Daily sales trend
        List<Object[]> dailySales = orderRepository.getDailyOrderStatistics(startDate, endDate);
        report.put("dailySalesTrend", dailySales);
        
        // Top performing products
        Page<Object[]> topProducts = orderRepository.findPopularProducts(startDate, endDate, PageRequest.of(0, 10));
        report.put("topProducts", topProducts.getContent());
        
        // Sales by category
        List<Object[]> categoryPerformance = orderRepository.getCategoryPerformance(startDate, endDate);
        report.put("categoryPerformance", categoryPerformance);
        
        return report;
    }
    
    private Map<String, Object> generateCustomerInsightsReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Customer acquisition
        report.put("newCustomers", userRepository.countNewCustomers(startDate, endDate));
        report.put("returningCustomers", orderRepository.countReturningCustomers(startDate, endDate));
        
        // Top customers
        Page<Object[]> topCustomers = orderRepository.findTopCustomersBySpendingInPeriod(startDate, endDate, PageRequest.of(0, 10));
        report.put("topCustomers", topCustomers.getContent());
        
        // Customer retention metrics
        report.put("repeatPurchaseRate", orderRepository.calculateRepeatPurchaseRate(startDate, endDate));
        
        return report;
    }
    
    private Map<String, Object> generateProductPerformanceReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Best sellers
        Page<Object[]> bestSellers = orderRepository.findPopularProducts(startDate, endDate, PageRequest.of(0, 20));
        report.put("bestSellers", bestSellers.getContent());
        
        // Slow movers
        Page<Object[]> slowMovers = orderRepository.findLowPerformingProducts(startDate, endDate, PageRequest.of(0, 20));
        report.put("slowMovers", slowMovers.getContent());
        
        // Inventory turnover
        report.put("inventoryTurnover", calculateInventoryTurnover(startDate, endDate));
        
        return report;
    }
    
    private Map<String, Object> generateOperationalMetricsReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Order fulfillment metrics
        Map<String, Object> fulfillmentMetrics = new HashMap<>();
        fulfillmentMetrics.put("averageProcessingTime", calculateAverageProcessingTime(startDate, endDate));
        fulfillmentMetrics.put("onTimeDeliveryRate", calculateOnTimeDeliveryRate(startDate, endDate));
        report.put("fulfillmentMetrics", fulfillmentMetrics);
        
        // Order status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusDistribution.put(status.name(), orderRepository.countByStatusAndDateRange(status, startDate, endDate));
        }
        report.put("orderStatusDistribution", statusDistribution);
        
        return report;
    }
    
    private Map<String, Object> calculateKeyPerformanceIndicators() {
        Map<String, Object> kpis = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        
        // Monthly targets vs actual (these would typically come from configuration)
        BigDecimal monthlyRevenueTarget = new BigDecimal("10000000"); // 10M VND
        BigDecimal actualRevenue = orderRepository.calculateRevenueBetweenDates(monthStart, now);
        
        kpis.put("revenueTarget", monthlyRevenueTarget);
        kpis.put("actualRevenue", actualRevenue);
        kpis.put("revenueAchievement", actualRevenue.divide(monthlyRevenueTarget, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        
        // Customer satisfaction (placeholder - would integrate with review system)
        kpis.put("customerSatisfactionScore", 4.2);
        
        // Inventory health
        kpis.put("lowStockItems", productRepository.countLowStockProducts());
        kpis.put("outOfStockItems", productRepository.countOutOfStockProducts());
        
        return kpis;
    }
    
    private Double calculateInventoryTurnover(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified inventory turnover calculation
        // In a real system, this would be more sophisticated
        return 2.5; // Placeholder
    }
    
    private Double calculateAverageProcessingTime(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate average time from order creation to shipment
        // This would require more complex queries
        return 2.3; // Placeholder - days
    }
    
    private Double calculateOnTimeDeliveryRate(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate percentage of orders delivered on time
        // This would require delivery date tracking
        return 92.5; // Placeholder - percentage
    }
    
    private OrderDto convertToOrderDto(Order order) {
        // Use the existing conversion method from OrderService
        return orderService.getOrderById(order.getId());
    }
    
    private UserDto convertToUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}