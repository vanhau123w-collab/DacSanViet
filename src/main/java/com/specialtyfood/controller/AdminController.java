package com.specialtyfood.controller;

import com.specialtyfood.dao.*;
import com.specialtyfood.dto.UpdateOrderStatusRequest;
import com.specialtyfood.model.OrderStatus;
// Removed Role import - using admin boolean instead
import com.specialtyfood.service.OrderService;
import com.specialtyfood.service.UserService;
import com.specialtyfood.service.ProductService;
import com.specialtyfood.service.InventoryService;
import com.specialtyfood.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller for administrative operations
 * Provides endpoints for order management dashboard, customer information views, and sales reporting
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final AdminService adminService;
    
    @Autowired
    public AdminController(OrderService orderService, 
                          UserService userService,
                          ProductService productService,
                          InventoryService inventoryService,
                          AdminService adminService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.adminService = adminService;
    }
    
    // ===== ORDER MANAGEMENT DASHBOARD =====
    
    /**
     * Get admin dashboard overview with key metrics
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        try {
            Map<String, Object> dashboard = adminService.generateDashboardMetrics();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load dashboard data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get all orders with filtering and pagination
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDao>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDao> orders;
            
            // Apply filters based on parameters
            if (status != null && startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                orders = orderService.getOrdersByStatusAndDateRange(status, startDateTime, endDateTime, pageable);
            } else if (status != null) {
                orders = orderService.getOrdersByStatus(status, pageable);
            } else if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                orders = orderService.getOrdersByDateRange(startDateTime, endDateTime, pageable);
            } else {
                orders = orderService.getAllOrders(pageable);
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search orders by various criteria
     * Requirements: 5.3 - Search for specific orders
     */
    @GetMapping("/orders/search")
    public ResponseEntity<Page<OrderDao>> searchOrders(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDao> orders = orderService.searchOrders(searchTerm, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get order details by ID
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDao> getOrderDetails(@PathVariable Long orderId) {
        try {
            OrderDao order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update order status
     * Requirements: 5.2 - Order status update with notifications
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderDao> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            OrderDao updatedOrder = adminService.updateOrderStatusWithNotifications(orderId, request, adminUsername);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approve COD order - change status from PROCESSING to SHIPPED
     * Requirements: COD workflow management
     */
    @PostMapping("/orders/{orderId}/approve-cod")
    public ResponseEntity<Map<String, Object>> approveCODOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderDao order = orderService.getOrderById(orderId);
            
            // Validate it's a COD order in PROCESSING status
            if (!"COD".equals(order.getPaymentMethod())) {
                response.put("success", false);
                response.put("message", "Đây không phải đơn hàng COD");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.PENDING) {
                response.put("success", false);
                response.put("message", "Đơn hàng phải ở trạng thái PENDING hoặc PROCESSING để phê duyệt COD");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract shipping carrier and tracking info from request
            String shippingCarrier = null;
            String trackingNumber = null;
            String notes = "Đơn hàng COD đã được phê duyệt và chuyển giao cho đơn vị vận chuyển";
            
            if (requestBody != null) {
                shippingCarrier = (String) requestBody.get("shippingCarrier");
                trackingNumber = (String) requestBody.get("trackingNumber");
                String customNotes = (String) requestBody.get("notes");
                if (customNotes != null && !customNotes.trim().isEmpty()) {
                    notes = customNotes;
                }
            }
            
            // Update status to SHIPPED
            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus(OrderStatus.SHIPPED);
            updateRequest.setNotes(notes);
            
            if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
                updateRequest.setTrackingNumber(trackingNumber);
            }
            
            OrderDao updatedOrder = orderService.updateOrderStatus(orderId, updateRequest);
            
            response.put("success", true);
            response.put("message", "Đơn hàng COD đã được phê duyệt thành công");
            response.put("order", updatedOrder);
            response.put("shippingCarrier", shippingCarrier);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi phê duyệt đơn hàng: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ===== CUSTOMER INFORMATION VIEWS =====
    
    /**
     * Get all customers with pagination and search
     * Requirements: 5.4 - Customer information display
     */
    @GetMapping("/customers")
    public ResponseEntity<Page<UserDao>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String searchTerm) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDao> customers;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                customers = userService.searchUsers(searchTerm, pageable);
            } else {
                customers = userService.getAllUsers(pageable);
            }
            
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get customer details with order history
     * Requirements: 5.4 - Customer information display with order history
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerDetails(@PathVariable Long customerId) {
        try {
            Map<String, Object> customerDetails = adminService.getCustomerAnalytics(customerId);
            return ResponseEntity.ok(customerDetails);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get customer's complete order history
     * Requirements: 5.4 - Customer information display
     */
    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<Page<OrderDao>> getCustomerOrderHistory(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDao> orders = orderService.getOrdersByUser(customerId, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Toggle customer account status (activate/deactivate)
     * Requirements: 5.4 - Customer management operations
     */
    @PutMapping("/customers/{customerId}/toggle-status")
    public ResponseEntity<UserDao> toggleCustomerStatus(@PathVariable Long customerId) {
        try {
            UserDao updatedCustomer = userService.toggleUserStatus(customerId);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== SALES REPORTING FUNCTIONALITY =====
    
    /**
     * Get comprehensive sales report
     * Requirements: 5.5 - Sales report with analytics on revenue, popular products, and customer trends
     */
    @GetMapping("/reports/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        try {
            Map<String, Object> salesReport = new HashMap<>();
            
            // Set default date range if not provided
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Revenue analytics
            Map<String, Object> revenueAnalytics = orderService.getRevenueAnalytics(startDateTime, endDateTime, period);
            salesReport.put("revenueAnalytics", revenueAnalytics);
            
            // Popular products
            Page<ProductDao> popularProducts = orderService.getPopularProducts(startDateTime, endDateTime, PageRequest.of(0, 10));
            salesReport.put("popularProducts", popularProducts.getContent());
            
            // Customer trends
            Map<String, Object> customerTrends = orderService.getCustomerTrends(startDateTime, endDateTime);
            salesReport.put("customerTrends", customerTrends);
            
            // Order status breakdown
            Map<String, Long> orderStatusBreakdown = orderService.getOrderStatusBreakdown(startDateTime, endDateTime);
            salesReport.put("orderStatusBreakdown", orderStatusBreakdown);
            
            // Top customers by spending
            Page<UserDao> topCustomers = orderService.getTopCustomersBySpending(startDateTime, endDateTime, PageRequest.of(0, 10));
            salesReport.put("topCustomers", topCustomers.getContent());
            
            // Report metadata
            Map<String, Object> reportMetadata = new HashMap<>();
            reportMetadata.put("startDate", startDate);
            reportMetadata.put("endDate", endDate);
            reportMetadata.put("period", period);
            reportMetadata.put("generatedAt", LocalDateTime.now());
            salesReport.put("metadata", reportMetadata);
            
            return ResponseEntity.ok(salesReport);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate sales report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get revenue analytics by period
     * Requirements: 5.5 - Revenue analytics
     */
    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String groupBy) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            Map<String, Object> revenueReport = orderService.getRevenueAnalytics(startDateTime, endDateTime, groupBy);
            return ResponseEntity.ok(revenueReport);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate revenue report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get product performance report
     * Requirements: 5.5 - Popular products analytics
     */
    @GetMapping("/reports/products")
    public ResponseEntity<Map<String, Object>> getProductPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            Map<String, Object> productReport = new HashMap<>();
            
            // Popular products by sales volume
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDao> popularProducts = orderService.getPopularProducts(startDateTime, endDateTime, pageable);
            productReport.put("popularProducts", popularProducts);
            
            // Product category performance
            Map<String, Object> categoryPerformance = orderService.getCategoryPerformance(startDateTime, endDateTime);
            productReport.put("categoryPerformance", categoryPerformance);
            
            // Low performing products
            Page<ProductDao> lowPerformingProducts = orderService.getLowPerformingProducts(startDateTime, endDateTime, pageable);
            productReport.put("lowPerformingProducts", lowPerformingProducts);
            
            return ResponseEntity.ok(productReport);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate product performance report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get customer analytics report
     * Requirements: 5.5 - Customer trends analytics
     */
    @GetMapping("/reports/customers")
    public ResponseEntity<Map<String, Object>> getCustomerAnalyticsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            Map<String, Object> customerReport = new HashMap<>();
            
            // Customer trends
            Map<String, Object> customerTrends = orderService.getCustomerTrends(startDateTime, endDateTime);
            customerReport.put("customerTrends", customerTrends);
            
            // Top customers by spending
            Page<UserDao> topCustomers = orderService.getTopCustomersBySpending(startDateTime, endDateTime, PageRequest.of(0, 20));
            customerReport.put("topCustomers", topCustomers);
            
            // New vs returning customers
            Map<String, Object> customerSegmentation = orderService.getCustomerSegmentation(startDateTime, endDateTime);
            customerReport.put("customerSegmentation", customerSegmentation);
            
            // Customer retention metrics
            Map<String, Object> retentionMetrics = orderService.getCustomerRetentionMetrics(startDateTime, endDateTime);
            customerReport.put("retentionMetrics", retentionMetrics);
            
            return ResponseEntity.ok(customerReport);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate customer analytics report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    // ===== ADDITIONAL ADMIN OPERATIONS =====
    
    /**
     * Bulk update order status
     * Requirements: 5.2 - Order management operations
     */
    @PutMapping("/orders/bulk-status-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateOrderStatus(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> orderIds = (List<Long>) request.get("orderIds");
            String statusStr = (String) request.get("status");
            OrderStatus newStatus = OrderStatus.valueOf(statusStr);
            String adminUsername = authentication.getName();
            
            Map<String, Object> result = adminService.bulkUpdateOrderStatus(orderIds, newStatus, adminUsername);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to bulk update orders: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Manage customer account status
     * Requirements: 5.3 - Customer management operations
     */
    @PutMapping("/customers/{customerId}/manage-status")
    public ResponseEntity<UserDao> manageCustomerStatus(
            @PathVariable Long customerId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Boolean activate = (Boolean) request.get("activate");
            String reason = (String) request.get("reason");
            String adminUsername = authentication.getName();
            
            UserDao updatedCustomer = adminService.manageCustomerStatus(customerId, activate, reason, adminUsername);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Advanced customer search
     * Requirements: 5.3 - Search for specific customers
     */
    @GetMapping("/customers/advanced-search")
    public ResponseEntity<Page<UserDao>> advancedCustomerSearch(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registeredAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registeredBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Boolean adminFilter = role != null ? "ADMIN".equalsIgnoreCase(role) : null;
            
            Page<UserDao> customers = adminService.searchCustomersAdvanced(
                searchTerm, adminFilter, isActive, registeredAfter, registeredBefore, pageable);
            
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate business intelligence report
     * Requirements: 5.5 - Analytics and reporting services
     */
    @GetMapping("/reports/business-intelligence")
    public ResponseEntity<Map<String, Object>> getBusinessIntelligenceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            Map<String, Object> report = adminService.generateBusinessIntelligenceReport(startDateTime, endDateTime);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate business intelligence report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}