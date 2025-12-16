package com.specialtyfood.repository;

import com.specialtyfood.model.Category;
import com.specialtyfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by name
     */
    Optional<Product> findByName(String name);
    
    /**
     * Check if product name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active products
     */
    Page<Product> findByIsActiveTrueOrderByName(Pageable pageable);
    
    /**
     * Find products by category
     */
    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);
    
    /**
     * Find products by category ID
     */
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    
    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search products by name, description, or category with category filter
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProductsInCategory(@Param("searchTerm") String searchTerm, 
                                          @Param("categoryId") Long categoryId, 
                                          Pageable pageable);
    
    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);
    
    /**
     * Find featured products
     */
    Page<Product> findByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find products in stock
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0")
    Page<Product> findInStockProducts(Pageable pageable);
    
    /**
     * Find low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity <= :threshold AND p.stockQuantity > 0")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    /**
     * Find out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    /**
     * Find products by origin
     */
    Page<Product> findByOriginAndIsActiveTrueOrderByName(String origin, Pageable pageable);
    
    /**
     * Find products by weight range
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "p.weightGrams BETWEEN :minWeight AND :maxWeight")
    Page<Product> findByWeightRange(@Param("minWeight") Integer minWeight, 
                                   @Param("maxWeight") Integer maxWeight, 
                                   Pageable pageable);
    
    /**
     * Find most popular products (by order count)
     */
    @Query("SELECT p, COUNT(oi) as orderCount FROM Product p " +
           "JOIN p.orderItems oi " +
           "WHERE p.isActive = true " +
           "GROUP BY p " +
           "ORDER BY COUNT(oi) DESC")
    Page<Object[]> findMostPopularProducts(Pageable pageable);
    
    /**
     * Find recently added products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findRecentProducts(Pageable pageable);
    
    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Find products with similar names (for recommendations)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.id != :productId AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findSimilarProducts(@Param("productId") Long productId, 
                                     @Param("keyword") String keyword, 
                                     Pageable pageable);
    
    /**
     * Find products in same category (for recommendations)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.id != :productId AND " +
           "p.category.id = :categoryId ORDER BY p.createdAt DESC")
    List<Product> findProductsInSameCategory(@Param("productId") Long productId, 
                                           @Param("categoryId") Long categoryId, 
                                           Pageable pageable);
    
    // Inventory management methods
    
    /**
     * Find products with stock quantity less than threshold
     */
    Page<Product> findByStockQuantityLessThanAndIsActiveTrue(Integer threshold, Pageable pageable);
    
    /**
     * Find products with stock quantity less than threshold (list)
     */
    List<Product> findByStockQuantityLessThanAndIsActiveTrue(Integer threshold);
    
    /**
     * Find products with exact stock quantity
     */
    Page<Product> findByStockQuantityAndIsActiveTrue(Integer stockQuantity, Pageable pageable);
    
    /**
     * Count active products
     */
    Long countByIsActiveTrue();
    
    /**
     * Count products with stock quantity less than threshold
     */
    Long countByStockQuantityLessThanAndIsActiveTrue(Integer threshold);
    
    /**
     * Count products with exact stock quantity
     */
    Long countByStockQuantityAndIsActiveTrue(Integer stockQuantity);
    
    /**
     * Calculate total stock value
     */
    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.isActive = true")
    Long calculateTotalStockValue();
    
    /**
     * Count low stock products (stock <= 10)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.stockQuantity <= 10 AND p.stockQuantity > 0")
    Long countLowStockProducts();
    
    /**
     * Count out of stock products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.stockQuantity = 0")
    Long countOutOfStockProducts();
}