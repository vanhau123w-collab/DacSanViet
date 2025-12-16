package com.specialtyfood.repository;

import com.specialtyfood.model.Order;
import com.specialtyfood.model.OrderStatus;
import com.specialtyfood.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);
    
    /**
     * Find orders by user
     */
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    
    /**
     * Find orders by user ID
     */
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    
    /**
     * Find orders by status
     */
    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by user and status
     */
    Page<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by user ID and status
     */
    Page<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, OrderStatus status, Pageable pageable);
    
    /**
     * Find orders within date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate, 
                                      Pageable pageable);
    
    /**
     * Search orders by order number, user name, or email
     */
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find orders by tracking number
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatusOrderByOrderDateDesc(String paymentStatus, Pageable pageable);
    
    /**
     * Find orders by total amount range
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByTotalAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                      @Param("maxAmount") BigDecimal maxAmount, 
                                      Pageable pageable);
    
    /**
     * Count orders by status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    /**
     * Count orders for user
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Calculate total revenue
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();
    
    /**
     * Calculate revenue within date range
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE " +
           "o.status != 'CANCELLED' AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recent orders (last N days)
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :sinceDate ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find pending orders older than specified time
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderDate < :cutoffDate")
    List<Order> findOldPendingOrders(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find orders that need shipping (confirmed status)
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' ORDER BY o.orderDate ASC")
    List<Order> findOrdersReadyForShipping();
    
    /**
     * Find shipped orders without delivery date
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND o.deliveredDate IS NULL " +
           "ORDER BY o.shippedDate ASC")
    List<Order> findShippedOrdersAwaitingDelivery();
    
    /**
     * Get monthly order statistics
     */
    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status != 'CANCELLED' " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY YEAR(o.orderDate) DESC, MONTH(o.orderDate) DESC")
    List<Object[]> getMonthlyOrderStatistics();
    
    /**
     * Get daily order statistics for date range
     */
    @Query("SELECT DATE(o.orderDate), COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status != 'CANCELLED' " +
           "AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.orderDate) " +
           "ORDER BY DATE(o.orderDate) DESC")
    List<Object[]> getDailyOrderStatistics(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find top customers by order count
     */
    @Query("SELECT o.user, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSpent " +
           "FROM Order o WHERE o.status != 'CANCELLED' " +
           "GROUP BY o.user " +
           "ORDER BY COUNT(o) DESC")
    Page<Object[]> findTopCustomersByOrderCount(Pageable pageable);
    
    /**
     * Find top customers by total spent
     */
    @Query("SELECT o.user, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSpent " +
           "FROM Order o WHERE o.status != 'CANCELLED' " +
           "GROUP BY o.user " +
           "ORDER BY SUM(o.totalAmount) DESC")
    Page<Object[]> findTopCustomersByTotalSpent(Pageable pageable);
    
    // ===== ADMIN ANALYTICS QUERIES =====
    
    /**
     * Find orders by status and date range
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status " +
           "AND o.orderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findOrdersByStatusAndDateRange(@Param("status") OrderStatus status,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);
    
    /**
     * Calculate total spent by user
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.user.id = :userId AND o.status != 'CANCELLED'")
    BigDecimal calculateTotalSpentByUser(@Param("userId") Long userId);
    
    /**
     * Calculate average order value by user
     */
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o " +
           "WHERE o.user.id = :userId AND o.status != 'CANCELLED'")
    BigDecimal calculateAverageOrderValueByUser(@Param("userId") Long userId);
    
    /**
     * Find popular products by sales volume
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status != 'CANCELLED' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product " +
           "ORDER BY SUM(oi.quantity) DESC")
    Page<Object[]> findPopularProducts(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);
    
    /**
     * Count returning customers in period
     */
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "AND o.user.id IN (" +
           "  SELECT DISTINCT o2.user.id FROM Order o2 " +
           "  WHERE o2.orderDate < :startDate" +
           ")")
    Long countReturningCustomers(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate average order frequency
     */
    @Query("SELECT AVG(userOrderCount) FROM (" +
           "  SELECT COUNT(o) as userOrderCount FROM Order o " +
           "  WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "  AND o.status != 'CANCELLED' " +
           "  GROUP BY o.user.id" +
           ") as subquery")
    Double calculateAverageOrderFrequency(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count orders by status and date range
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") OrderStatus status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find top customers by spending in period
     */
    @Query("SELECT o.user, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSpent " +
           "FROM Order o WHERE o.status != 'CANCELLED' " +
           "AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.user " +
           "ORDER BY SUM(o.totalAmount) DESC")
    Page<Object[]> findTopCustomersBySpendingInPeriod(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);
    
    /**
     * Get category performance
     */
    @Query("SELECT c.name, SUM(oi.quantity) as totalSold, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.product p JOIN p.category c JOIN oi.order o " +
           "WHERE o.status != 'CANCELLED' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.id, c.name " +
           "ORDER BY SUM(oi.quantity * oi.unitPrice) DESC")
    List<Object[]> getCategoryPerformance(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find low performing products
     */
    @Query("SELECT p, COALESCE(SUM(oi.quantity), 0) as totalSold " +
           "FROM Product p LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
           "LEFT JOIN Order o ON oi.order.id = o.id " +
           "WHERE (o IS NULL OR (o.status != 'CANCELLED' AND o.orderDate BETWEEN :startDate AND :endDate)) " +
           "GROUP BY p.id " +
           "ORDER BY COALESCE(SUM(oi.quantity), 0) ASC")
    Page<Object[]> findLowPerformingProducts(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);
    
    /**
     * Get customer lifetime value segments (native query)
     */
    @Query(value = "SELECT segment, COUNT(*) as customerCount " +
           "FROM (" +
           "  SELECT " +
           "    CASE " +
           "      WHEN SUM(total_amount) >= 5000000 THEN 'High Value' " +
           "      WHEN SUM(total_amount) >= 1000000 THEN 'Medium Value' " +
           "      ELSE 'Low Value' " +
           "    END as segment " +
           "  FROM orders " +
           "  WHERE status != 'CANCELLED' " +
           "  GROUP BY user_id" +
           ") customer_segments " +
           "GROUP BY segment", 
           nativeQuery = true)
    List<Object[]> getCustomerLifetimeValueSegments();
    
    /**
     * Calculate repeat purchase rate (native query)
     */
    @Query(value = "SELECT " +
           "CASE WHEN COUNT(DISTINCT user_id) > 0 THEN " +
           "  CAST(SUM(CASE WHEN order_count > 1 THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(DISTINCT user_id) * 100 " +
           "ELSE 0.0 END " +
           "FROM (SELECT user_id, COUNT(*) as order_count " +
           "      FROM orders " +
           "      WHERE order_date BETWEEN ?1 AND ?2 AND status != 'CANCELLED' " +
           "      GROUP BY user_id) user_orders", 
           nativeQuery = true)
    Double calculateRepeatPurchaseRate(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate customer churn rate (native query)
     */
    @Query(value = "SELECT " +
           "CASE WHEN active_customers > 0 THEN " +
           "  CAST((active_customers - current_customers) AS DOUBLE) / active_customers * 100 " +
           "ELSE 0.0 END " +
           "FROM (" +
           "  SELECT " +
           "    COUNT(DISTINCT o1.user_id) as active_customers, " +
           "    COUNT(DISTINCT o2.user_id) as current_customers " +
           "  FROM (SELECT DISTINCT user_id FROM orders WHERE order_date < ?1 AND status != 'CANCELLED') o1 " +
           "  LEFT JOIN (SELECT DISTINCT user_id FROM orders WHERE order_date BETWEEN ?1 AND ?2 AND status != 'CANCELLED') o2 " +
           "    ON o1.user_id = o2.user_id" +
           ") churn_calc", 
           nativeQuery = true)
    Double calculateCustomerChurnRate(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate average customer lifespan (native query)
     */
    @Query(value = "SELECT AVG(DATEDIFF(last_order, first_order)) " +
           "FROM (" +
           "  SELECT " +
           "    user_id, " +
           "    MIN(order_date) as first_order, " +
           "    MAX(order_date) as last_order " +
           "  FROM orders " +
           "  WHERE status != 'CANCELLED' " +
           "  GROUP BY user_id " +
           "  HAVING COUNT(*) > 1 AND DATEDIFF(MAX(order_date), MIN(order_date)) > 0" +
           ") customer_lifespans", 
           nativeQuery = true)
    Double calculateAverageCustomerLifespan();
    
    /**
     * Count orders by user ID and status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    /**
     * Count orders between dates
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate average order value in period
     */
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status != 'CANCELLED' AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateAverageOrderValueInPeriod(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}