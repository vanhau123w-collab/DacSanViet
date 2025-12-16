package com.specialtyfood.repository;

import com.specialtyfood.config.QueryOptimizationConfig;
import com.specialtyfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.List;

/**
 * Optimized repository interface for Product entity with performance enhancements.
 * This repository extends ProductRepository with additional optimized queries.
 */
@Repository
public interface OptimizedProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Optimized search for products with caching and performance hints.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
           "WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHEABLE, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHE_REGION, 
                   value = "productSearch"),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.FETCH_SIZE, 
                   value = QueryOptimizationConfig.QueryHints.DEFAULT_FETCH_SIZE)
    })
    Page<Product> searchProductsOptimized(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Optimized featured products query with caching.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category " +
           "WHERE p.isActive = true AND p.isFeatured = true " +
           "ORDER BY p.createdAt DESC")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHEABLE, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHE_REGION, 
                   value = "featuredProducts"),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE)
    })
    Page<Product> findFeaturedProductsOptimized(Pageable pageable);
    
    /**
     * Optimized category products query with performance hints.
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.isActive = true AND p.category.id = :categoryId " +
           "ORDER BY p.name")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHEABLE, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.FETCH_SIZE, 
                   value = QueryOptimizationConfig.QueryHints.DEFAULT_FETCH_SIZE)
    })
    Page<Product> findByCategoryOptimized(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Optimized price range query with index hints.
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY p.price ASC")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.FETCH_SIZE, 
                   value = QueryOptimizationConfig.QueryHints.DEFAULT_FETCH_SIZE)
    })
    Page<Product> findByPriceRangeOptimized(@Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice, 
                                           Pageable pageable);
    
    /**
     * Optimized inventory report query.
     */
    @Query("SELECT p.id, p.name, p.stockQuantity, p.price, " +
           "p.stockQuantity * p.price as stockValue " +
           "FROM Product p " +
           "WHERE p.isActive = true " +
           "ORDER BY p.stockQuantity ASC")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.FETCH_SIZE, 
                   value = "100")
    })
    List<Object[]> getInventoryReport();
    
    /**
     * Optimized low stock products with specific projection.
     */
    @Query("SELECT p.id, p.name, p.stockQuantity, p.category.name " +
           "FROM Product p " +
           "WHERE p.isActive = true AND p.stockQuantity <= :threshold AND p.stockQuantity > 0 " +
           "ORDER BY p.stockQuantity ASC")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE)
    })
    List<Object[]> findLowStockProductsProjection(@Param("threshold") Integer threshold);
    
    /**
     * Optimized popular products query with aggregation.
     */
    @Query("SELECT p, COUNT(oi) as orderCount, SUM(oi.quantity) as totalSold " +
           "FROM Product p " +
           "JOIN p.orderItems oi " +
           "JOIN oi.order o " +
           "WHERE p.isActive = true AND o.status IN ('DELIVERED', 'SHIPPED') " +
           "GROUP BY p " +
           "ORDER BY COUNT(oi) DESC, SUM(oi.quantity) DESC")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHEABLE, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHE_REGION, 
                   value = "popularProducts"),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE)
    })
    Page<Object[]> findPopularProductsOptimized(Pageable pageable);
    
    /**
     * Optimized product statistics query.
     */
    @Query("SELECT " +
           "COUNT(p) as totalProducts, " +
           "COUNT(CASE WHEN p.stockQuantity > 0 THEN 1 END) as inStockProducts, " +
           "COUNT(CASE WHEN p.stockQuantity = 0 THEN 1 END) as outOfStockProducts, " +
           "COUNT(CASE WHEN p.stockQuantity <= 10 AND p.stockQuantity > 0 THEN 1 END) as lowStockProducts, " +
           "AVG(p.price) as averagePrice, " +
           "SUM(p.stockQuantity * p.price) as totalStockValue " +
           "FROM Product p " +
           "WHERE p.isActive = true")
    @QueryHints({
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHEABLE, 
                   value = QueryOptimizationConfig.QueryHints.TRUE),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.CACHE_REGION, 
                   value = "productStats"),
        @QueryHint(name = QueryOptimizationConfig.QueryHints.READ_ONLY, 
                   value = QueryOptimizationConfig.QueryHints.TRUE)
    })
    Object[] getProductStatistics();
}