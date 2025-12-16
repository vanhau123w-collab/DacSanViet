package com.specialtyfood.controller;

import com.specialtyfood.dto.*;
import com.specialtyfood.model.OrderStatus;
import com.specialtyfood.service.AdminService;
import com.specialtyfood.service.OrderService;
import com.specialtyfood.service.ProductService;
import com.specialtyfood.service.UserService;
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
    
    @Autowired
    public AdminViewController(AdminService adminService, 
                              OrderService orderService,
                              ProductService productService,
                              UserService userService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }
    
    /**
     * Admin dashboard homepage
     * Requirements: 5.1 - Order management dashboard display
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Get dashboard metrics
            Map<String, Object> dashboardMetrics = adminService.generateDashboardMetrics();
            model.addAttribute("statistics", dashboardMetrics.get("statistics"));
            
            // Get recent orders (last 10)
            Pageable recentOrdersPageable = PageRequest.of(0, 10, Sort.by("orderDate").descending());
            Page<OrderDto> recentOrdersPage = orderService.getAllOrders(recentOrdersPageable);
            model.addAttribute("recentOrders", recentOrdersPage.getContent());
            
            // Get low stock products
            @SuppressWarnings("unchecked")
            List<ProductDto> lowStockProducts = (List<ProductDto>) dashboardMetrics.get("lowStockProducts");
            model.addAttribute("lowStockProducts", lowStockProducts);
            
            // Get pending orders count
            Long pendingOrdersCount = orderService.countOrdersByStatus(OrderStatus.PENDING);
            model.addAttribute("pendingOrdersCount", pendingOrdersCount);
            
            // Get top selling products
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = LocalDate.now().atTime(23, 59, 59);
            Page<ProductDto> topProductsPage = orderService.getPopularProducts(startOfMonth, endOfMonth, PageRequest.of(0, 5));
            model.addAttribute("topProducts", topProductsPage.getContent());
            
            return "admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "admin/dashboard";
        }
    }
    
    /**
     * Order management page
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders")
    public String orders(Model model,
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
            
            Page<OrderDto> orders;
            
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
            
            model.addAttribute("orders", orders);
            model.addAttribute("currentStatus", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            // Add order status options for filter
            model.addAttribute("orderStatuses", OrderStatus.values());
            
            return "admin/orders/list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách đơn hàng: " + e.getMessage());
            return "admin/orders/list";
        }
    }
    
    /**
     * Order details page
     * Requirements: 5.1 - Order management dashboard
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Model model) {
        try {
            OrderDto order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "admin/orders/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết đơn hàng: " + e.getMessage());
            return "admin/orders/detail";
        }
    }
    
    /**
     * Customer management page
     * Requirements: 5.4 - Customer information display
     */
    @GetMapping("/customers")
    public String customers(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "createdAt") String sortBy,
                           @RequestParam(defaultValue = "desc") String sortDir,
                           @RequestParam(required = false) String searchTerm) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDto> customers;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                customers = userService.searchUsers(searchTerm, pageable);
                model.addAttribute("searchTerm", searchTerm);
            } else {
                customers = userService.getAllUsers(pageable);
            }
            
            model.addAttribute("customers", customers);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            return "admin/customers/list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách khách hàng: " + e.getMessage());
            return "admin/customers/list";
        }
    }
    
    /**
     * Customer details page
     * Requirements: 5.4 - Customer information display with order history
     */
    @GetMapping("/customers/{customerId}")
    public String customerDetails(@PathVariable Long customerId, Model model) {
        try {
            Map<String, Object> customerDetails = adminService.getCustomerAnalytics(customerId);
            model.addAttribute("customer", customerDetails.get("customer"));
            model.addAttribute("customerStats", customerDetails.get("statistics"));
            
            // Get customer's recent orders
            Page<OrderDto> recentOrders = orderService.getOrdersByUser(customerId, PageRequest.of(0, 10, Sort.by("orderDate").descending()));
            model.addAttribute("recentOrders", recentOrders.getContent());
            
            return "admin/customers/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin khách hàng: " + e.getMessage());
            return "admin/customers/detail";
        }
    }
    
    /**
     * Product management page
     * Requirements: 5.1 - Product management interface
     */
    @GetMapping("/products")
    public String products(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(defaultValue = "name") String sortBy,
                          @RequestParam(defaultValue = "asc") String sortDir,
                          @RequestParam(required = false) String searchTerm,
                          @RequestParam(required = false) Long categoryId) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductDto> products = productService.searchProducts(searchTerm, categoryId, pageable);
            
            model.addAttribute("products", products);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            return "admin/products/list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách sản phẩm: " + e.getMessage());
            return "admin/products/list";
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
            
            return "admin/reports/sales";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tạo báo cáo: " + e.getMessage());
            return "admin/reports/sales";
        }
    }
}