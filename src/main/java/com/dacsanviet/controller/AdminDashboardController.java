package com.dacsanviet.controller;

import com.dacsanviet.service.DashboardService;
import com.dacsanviet.service.OrderService;
import com.dacsanviet.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Admin Dashboard Controller
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private OrderService orderService;

    /**
     * Dashboard Overview Page
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard Overview");
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard/index";
    }

    /**
     * Get Dashboard Statistics (AJAX)
     */
    @GetMapping("/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String period) {
        
        Map<String, Object> stats = dashboardService.getDashboardStatistics(period);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get Sales Chart Data (AJAX)
     */
    @GetMapping("/dashboard/sales-chart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSalesChartData(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        Map<String, Object> chartData = dashboardService.getSalesChartData(startDate, endDate);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Get Top Products (AJAX)
     */
    @GetMapping("/dashboard/top-products")
    @ResponseBody
    public ResponseEntity<?> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));
    }

    /**
     * Get Recent Orders (AJAX) - with pagination
     */
    @GetMapping("/dashboard/recent-orders")
    @ResponseBody
    public ResponseEntity<?> getRecentOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        // Get recent orders with pagination info
        java.util.List<com.dacsanviet.dao.OrderDao> orders = dashboardService.getRecentOrders(size * 10); // Get more for pagination
        
        // Calculate pagination
        int totalOrders = orders.size();
        int totalPages = (int) Math.ceil((double) totalOrders / size);
        int start = page * size;
        int end = Math.min(start + size, totalOrders);
        
        // Get page subset
        java.util.List<com.dacsanviet.dao.OrderDao> pageOrders = 
            start < totalOrders ? orders.subList(start, end) : new java.util.ArrayList<>();
        
        // Build response
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", pageOrders);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalOrders);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Products Management Page
     */
    @GetMapping("/products")
    public String productsManagement(Model model) {
        model.addAttribute("pageTitle", "Products Management");
        model.addAttribute("activePage", "products");
        return "admin/products/index";
    }

    /**
     * Orders Management Page
     */
    @GetMapping("/orders")
    public String ordersManagement(Model model) {
        try {
            // Get orders with pagination (first page, 20 items)
            Pageable pageable = PageRequest.of(0, 20, Sort.by("orderDate").descending());
            Page<OrderDao> orders = orderService.getAllOrders(pageable);
            
            model.addAttribute("orders", orders);
            model.addAttribute("pageTitle", "Orders Management");
            model.addAttribute("activePage", "orders");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách đơn hàng: " + e.getMessage());
        }
        
        return "admin/orders/list-debug";
    }

    /**
     * Customers Management Page
     */
    @GetMapping("/customers")
    public String customersManagement(Model model) {
        model.addAttribute("pageTitle", "Customers Management");
        model.addAttribute("activePage", "customers");
        return "admin/customers/index";
    }

    /**
     * Analytics & Reports Page
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("pageTitle", "Analytics & Reports");
        model.addAttribute("activePage", "analytics");
        return "admin/analytics/index";
    }
}
