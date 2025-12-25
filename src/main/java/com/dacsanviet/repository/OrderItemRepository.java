package com.dacsanviet.repository;

import com.dacsanviet.model.Order;
import com.dacsanviet.model.OrderItem;
import com.dacsanviet.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for OrderItem entity operations
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find order items by order
     */
    List<OrderItem> findByOrderOrderByCreatedAt(Order order);
    
    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderIdOrderByCreatedAt(Long orderId);
    
    /**
     * Find order items by product
     */
    List<OrderItem> findByProductOrderByCreatedAtDesc(Product product);
    
    /**
     * Find order items by product ID
     */
    List<OrderItem> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    /**
     * Count order items for order
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Calculate total quantity for order
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Integer calculateTotalQuantityByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Calculate total value for order
     */
    @Query("SELECT COALESCE(SUM(oi.unitPrice * oi.quantity), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    BigDecimal calculateTotalValueByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Find best selling products
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold, COUNT(DISTINCT oi.order) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "GROUP BY oi.product " +
           "ORDER BY SUM(oi.quantity) DESC")
    Page<Object[]> findBestSellingProducts(Pageable pageable);
    
    /**
     * Find best selling products within date range
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold, COUNT(DISTINCT oi.order) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "AND oi.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product " +
           "ORDER BY SUM(oi.quantity) DESC")
    Page<Object[]> findBestSellingProductsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate, 
                                                      Pageable pageable);
    
    /**
     * Find products by revenue
     */
    @Query("SELECT oi.product, SUM(oi.unitPrice * oi.quantity) as totalRevenue, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "GROUP BY oi.product " +
           "ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    Page<Object[]> findProductsByRevenue(Pageable pageable);
    
    /**
     * Get product sales statistics
     */
    @Query("SELECT oi.productName, oi.categoryName, SUM(oi.quantity) as totalSold, " +
           "COUNT(DISTINCT oi.order) as orderCount, SUM(oi.unitPrice * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "GROUP BY oi.productName, oi.categoryName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getProductSalesStatistics();
    
    /**
     * Get category sales statistics
     */
    @Query("SELECT oi.categoryName, SUM(oi.quantity) as totalSold, " +
           "COUNT(DISTINCT oi.order) as orderCount, SUM(oi.unitPrice * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' AND oi.categoryName IS NOT NULL " +
           "GROUP BY oi.categoryName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getCategorySalesStatistics();
    
    /**
     * Find top categories by sales for charts (limit 5)
     */
    @Query("SELECT oi.categoryName, SUM(oi.unitPrice * oi.quantity) as totalRevenue, COUNT(DISTINCT oi.order) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != 'CANCELLED' AND oi.categoryName IS NOT NULL " +
           "GROUP BY oi.categoryName " +
           "ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    List<Object[]> findTopCategoriesBySales(Pageable pageable);
    
    /**
     * Find order items with quantity greater than threshold
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.quantity > :threshold ORDER BY oi.quantity DESC")
    List<OrderItem> findLargeQuantityOrders(@Param("threshold") Integer threshold);
    
    /**
     * Find order items with high value
     */
    @Query("SELECT oi FROM OrderItem oi WHERE (oi.unitPrice * oi.quantity) > :threshold " +
           "ORDER BY (oi.unitPrice * oi.quantity) DESC")
    List<OrderItem> findHighValueOrderItems(@Param("threshold") BigDecimal threshold);
    
    /**
     * Find order items by price range
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.unitPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY oi.unitPrice DESC")
    List<OrderItem> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                    @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * Find order items for user
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId ORDER BY oi.createdAt DESC")
    Page<OrderItem> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find recent order items for product
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId " +
           "AND oi.createdAt >= :sinceDate ORDER BY oi.createdAt DESC")
    List<OrderItem> findRecentOrderItemsByProductId(@Param("productId") Long productId, 
                                                   @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Calculate average order quantity for product
     */
    @Query("SELECT AVG(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId " +
           "AND oi.order.status != 'CANCELLED'")
    Double calculateAverageOrderQuantityByProductId(@Param("productId") Long productId);
    
    /**
     * Calculate average unit price for product
     */
    @Query("SELECT AVG(oi.unitPrice) FROM OrderItem oi WHERE oi.product.id = :productId " +
           "AND oi.order.status != 'CANCELLED'")
    BigDecimal calculateAverageUnitPriceByProductId(@Param("productId") Long productId);
    
    /**
     * Find customers who bought specific product
     */
    @Query("SELECT DISTINCT oi.order.user FROM OrderItem oi WHERE oi.product.id = :productId " +
           "AND oi.order.status != 'CANCELLED'")
    List<Object> findCustomersWhoBoughtProduct(@Param("productId") Long productId);
    
    /**
     * Find products frequently bought together
     */
    @Query("SELECT oi2.product, COUNT(*) as frequency FROM OrderItem oi1 " +
           "JOIN OrderItem oi2 ON oi1.order = oi2.order " +
           "WHERE oi1.product.id = :productId AND oi2.product.id != :productId " +
           "AND oi1.order.status != 'CANCELLED' " +
           "GROUP BY oi2.product " +
           "ORDER BY COUNT(*) DESC")
    Page<Object[]> findProductsFrequentlyBoughtTogether(@Param("productId") Long productId, Pageable pageable);
}