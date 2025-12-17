package com.specialtyfood.controller;

import com.specialtyfood.dao.*;
import com.specialtyfood.model.OrderStatus;
import com.specialtyfood.service.AdminService;
import com.specialtyfood.service.OrderService;
import com.specialtyfood.service.ProductService;
import com.specialtyfood.service.UserService;
import com.specialtyfood.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin view controller for serving Thymeleaf templates
 * Provides MVC endpoints for admin dashboard pages
 * Requirements: 5.1 - Admin dashboard interface
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {
    
    private final AdminService adminService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderRepository orderRepository;
    
    @Autowired
    public AdminViewController(AdminService adminService, 
                              OrderService orderService,
                              ProductService productService,
                              UserService userService,
                              OrderRepository orderRepository) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
        this.orderRepository = orderRepository;
    }
    
    /**
     * Test dashboard for debugging
     */
    @GetMapping("/dashboard-test")
    public String dashboardTest(Model model) {
        try {
            // Get basic statistics
            Long totalOrders = orderRepository.count(); // All orders
            Long pendingOrders = orderService.countOrdersByStatus(OrderStatus.PENDING);
            Long processingOrders = orderService.countOrdersByStatus(OrderStatus.PROCESSING);
            Long shippedOrders = orderService.countOrdersByStatus(OrderStatus.SHIPPED);
            
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("processingOrders", processingOrders);
            model.addAttribute("shippedOrders", shippedOrders);
            
            // Get recent orders
            Pageable recentOrdersPageable = PageRequest.of(0, 10, Sort.by("id").descending());
            Page<OrderDao> recentOrdersPage = orderService.getAllOrders(recentOrdersPageable);
            model.addAttribute("recentOrders", recentOrdersPage.getContent());
            
            return "admin/dashboard-test";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "admin/dashboard-test";
        }
    }

    /**
     * Simple admin dashboard for testing
     */
    @GetMapping("/dashboard-simple")
    public String dashboardSimple(Model model) {
        try {
            // Get basic statistics
            Long totalOrders = orderRepository.count(); // All orders
            Long pendingOrders = orderService.countOrdersByStatus(OrderStatus.PENDING);
            Long processingOrders = orderService.countOrdersByStatus(OrderStatus.PROCESSING);
            Long shippedOrders = orderService.countOrdersByStatus(OrderStatus.SHIPPED);
            
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("processingOrders", processingOrders);
            model.addAttribute("shippedOrders", shippedOrders);
            
            // Get recent orders
            Pageable recentOrdersPageable = PageRequest.of(0, 10, Sort.by("id").descending());
            Page<OrderDao> recentOrdersPage = orderService.getAllOrders(recentOrdersPageable);
            model.addAttribute("recentOrders", recentOrdersPage.getContent());
            
            return "admin/dashboard-simple";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "admin/dashboard-simple";
        }
    }

    /**
     * Admin dashboard homepage
     * Requirements: 5.1 - Order management dashboard display
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            System.out.println("Dashboard method called");
            
            // Set basic attributes to avoid null errors
            model.addAttribute("recentOrders", new java.util.ArrayList<>());
            model.addAttribute("pendingOrdersCount", 0L);
            model.addAttribute("processingOrdersCount", 0L);
            model.addAttribute("topProducts", new java.util.ArrayList<>());
            
            // Try to get dashboard metrics
            try {
                Map<String, Object> dashboardMetrics = adminService.generateDashboardMetrics();
                if (dashboardMetrics != null) {
                    model.addAttribute("statistics", dashboardMetrics.get("statistics"));
                    model.addAttribute("lowStockProducts", dashboardMetrics.get("lowStockProducts"));
                }
            } catch (Exception e) {
                System.err.println("Dashboard metrics error: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Try to get recent orders
            try {
                Pageable recentOrdersPageable = PageRequest.of(0, 10, Sort.by("id").descending());
                Page<OrderDao> recentOrdersPage = orderService.getAllOrders(recentOrdersPageable);
                model.addAttribute("recentOrders", recentOrdersPage.getContent());
            } catch (Exception e) {
                System.err.println("Recent orders error: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Try to get order counts
            try {
                Long pendingOrdersCount = orderService.countOrdersByStatus(OrderStatus.PENDING);
                Long processingOrdersCount = orderService.countOrdersByStatus(OrderStatus.PROCESSING);
                model.addAttribute("pendingOrdersCount", pendingOrdersCount);
                model.addAttribute("processingOrdersCount", processingOrdersCount);
            } catch (Exception e) {
                System.err.println("Order counts error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("Dashboard method completed successfully");
            return "admin/dashboard-simple-fixed";
            
        } catch (Exception e) {
            System.err.println("Dashboard method error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "admin/dashboard-simple-fixed";
        }
    }
    
    /**
     * Order management page
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders-test")
    public String ordersTest(Model model) {
        model.addAttribute("message", "Test page works!");
        return "admin/orders/list-test";
    }

    @GetMapping("/orders")
    public String orders(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir,
                        @RequestParam(required = false) OrderStatus status,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            System.out.println("Orders method called with params: page=" + page + ", size=" + size + ", sortBy=" + sortBy);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDao> orders;
            
            // Simplified - just get all orders first
            orders = orderService.getAllOrders(pageable);
            System.out.println("Retrieved " + orders.getTotalElements() + " orders");
            
            model.addAttribute("orders", orders);
            model.addAttribute("currentStatus", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            // Add order status options for filter
            model.addAttribute("orderStatuses", OrderStatus.values());
            
            return "admin/orders/list-debug";
        } catch (Exception e) {
            System.err.println("Error in orders method: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Không thể tải danh sách đơn hàng: " + e.getMessage());
            return "admin/orders/list-debug";
        }
    }
    
    /**
     * Order details page
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Model model) {
        try {
            OrderDao order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "admin/orders/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết đơn hàng: " + e.getMessage());
            return "admin/orders/detail";
        }
    }
    
    /**
     * Sales reports page
     * Requirements: 5.5 - Sales report with analytics
     */
    @GetMapping("/reports")
    public String reports(Model model,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                         @RequestParam(defaultValue = "MONTHLY") String period) {
        try {
            // Set default date range if not provided
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(3);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Generate comprehensive sales report
            Map<String, Object> salesReport = adminService.generateBusinessIntelligenceReport(startDateTime, endDateTime);
            
            model.addAttribute("salesReport", salesReport);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("period", period);
            
            return "admin/reports/sales-simple";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tạo báo cáo: " + e.getMessage());
            return "admin/reports/sales-simple";
        }
    }
}