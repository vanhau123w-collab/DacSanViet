package com.dacsanviet.controller;

import com.dacsanviet.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

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
     * Get Recent Orders (AJAX)
     */
    @GetMapping("/dashboard/recent-orders")
    @ResponseBody
    public ResponseEntity<?> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(limit));
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
        model.addAttribute("pageTitle", "Orders Management");
        model.addAttribute("activePage", "orders");
        return "admin/orders/index";
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
