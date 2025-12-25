package com.dacsanviet.service;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.model.Order;
import com.dacsanviet.model.OrderStatus;
import com.dacsanviet.model.Product;
import com.dacsanviet.repository.OrderItemRepository;
import com.dacsanviet.repository.OrderRepository;
import com.dacsanviet.repository.ProductRepository;
import com.dacsanviet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service for Admin Statistics
 */
@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Get Dashboard Statistics
     */
    public Map<String, Object> getDashboardStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startDate = getStartDateByPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();
        
        // Total Revenue
        BigDecimal totalRevenue = calculateTotalRevenue(startDate, endDate);
        BigDecimal previousRevenue = calculateTotalRevenue(
            startDate.minusDays(30), startDate);
        double revenueChange = calculatePercentageChange(previousRevenue, totalRevenue);
        
        stats.put("totalRevenue", totalRevenue);
        stats.put("revenueChange", revenueChange);
        
        // Total Orders
        long totalOrders = orderRepository.countByOrderDateBetween(startDate, endDate);
        long previousOrders = orderRepository.countByOrderDateBetween(
            startDate.minusDays(30), startDate);
        double ordersChange = calculatePercentageChange(
            BigDecimal.valueOf(previousOrders), BigDecimal.valueOf(totalOrders));
        
        stats.put("totalOrders", totalOrders);
        stats.put("ordersChange", ordersChange);
        
        // Average Order Value
        BigDecimal avgOrderValue = totalOrders > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
        
        stats.put("avgOrderValue", avgOrderValue);
        
        // New Customers
        long newCustomers = userRepository.countByCreatedAtBetween(startDate, endDate);
        long previousCustomers = userRepository.countByCreatedAtBetween(
            startDate.minusDays(30), startDate);
        double customersChange = calculatePercentageChange(
            BigDecimal.valueOf(previousCustomers), BigDecimal.valueOf(newCustomers));
        
        stats.put("newCustomers", newCustomers);
        stats.put("customersChange", customersChange);
        
        return stats;
    }

    /**
     * Get Sales Chart Data
     */
    public Map<String, Object> getSalesChartData(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        
        // Group by date
        Map<LocalDate, BigDecimal> dailySales = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getOrderDate().toLocalDate(),
                Collectors.reducing(BigDecimal.ZERO, 
                    Order::getTotalAmount, 
                    BigDecimal::add)
            ));
        
        // Prepare chart data
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            labels.add(current.toString());
            data.add(dailySales.getOrDefault(current, BigDecimal.ZERO));
            current = current.plusDays(1);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", data);
        
        return chartData;
    }

    /**
     * Get Top Selling Products
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        try {
            // Use PageRequest to limit results
            org.springframework.data.domain.PageRequest pageRequest = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            
            org.springframework.data.domain.Page<Object[]> results = 
                orderItemRepository.findBestSellingProducts(pageRequest);
            
            return results.getContent().stream()
                .map(row -> {
                    Product product = (Product) row[0];
                    Long totalSold = ((Number) row[1]).longValue();
                    Long orderCount = ((Number) row[2]).longValue();
                    
                    // Force load category to avoid lazy loading issue
                    String categoryName = "N/A";
                    if (product.getCategory() != null) {
                        categoryName = product.getCategory().getName();
                    }
                    
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", product.getId());
                    productData.put("name", product.getName());
                    productData.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "/images/placeholder.jpg");
                    productData.put("price", product.getPrice());
                    productData.put("stock", product.getStockQuantity());
                    productData.put("totalSold", totalSold);
                    productData.put("orderCount", orderCount);
                    productData.put("category", categoryName);
                    
                    return productData;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting top selling products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get Recent Orders (last 3 days only)
     */
    @Transactional(readOnly = true)
    public List<OrderDao> getRecentOrders(int limit) {
        try {
            // Get orders from last 3 days
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<Order> orders = orderRepository.findRecentOrders(threeDaysAgo);
            
            return orders.stream()
                .limit(limit)
                .map(order -> {
                    try {
                        // Force load orderItems to avoid LazyInitializationException
                        if (order.getOrderItems() != null) {
                            order.getOrderItems().size();
                        }
                        return orderService.convertToDao(order);
                    } catch (Exception e) {
                        System.err.println("Error converting order " + order.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(dao -> dao != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting recent orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper methods
    private LocalDateTime getStartDateByPeriod(String period) {
        if (period == null) period = "30days";
        
        switch (period) {
            case "7days":
                return LocalDateTime.now().minusDays(7);
            case "30days":
                return LocalDateTime.now().minusDays(30);
            case "90days":
                return LocalDateTime.now().minusDays(90);
            case "year":
                return LocalDateTime.now().minusYears(1);
            default:
                return LocalDateTime.now().minusDays(30);
        }
    }

    private BigDecimal calculateTotalRevenue(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream()
            .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change.divide(previous, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        return percentage.doubleValue();
    }
}
