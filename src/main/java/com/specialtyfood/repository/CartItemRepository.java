package com.specialtyfood.repository;

import com.specialtyfood.model.CartItem;
import com.specialtyfood.model.Product;
import com.specialtyfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Find cart items by user
     */
    List<CartItem> findByUserOrderByAddedDateDesc(User user);
    
    /**
     * Find cart items by user ID
     */
    List<CartItem> findByUserIdOrderByAddedDateDesc(Long userId);
    
    /**
     * Find specific cart item by user and product
     */
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    
    /**
     * Find specific cart item by user ID and product ID
     */
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * Check if cart item exists for user and product
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Check if cart item exists by user ID and product ID
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * Count cart items for user
     */
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Calculate total cart value for user
     */
    @Query("SELECT COALESCE(SUM(c.unitPrice * c.quantity), 0) FROM CartItem c WHERE c.user.id = :userId")
    BigDecimal calculateCartTotal(@Param("userId") Long userId);
    
    /**
     * Calculate total quantity in cart for user
     */
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.user.id = :userId")
    Integer calculateTotalQuantity(@Param("userId") Long userId);
    
    /**
     * Delete all cart items for user
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Delete cart item by user and product
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId AND c.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * Find cart items with products that are out of stock
     */
    @Query("SELECT c FROM CartItem c WHERE c.product.stockQuantity < c.quantity OR c.product.stockQuantity = 0")
    List<CartItem> findCartItemsWithInsufficientStock();
    
    /**
     * Find cart items for user with products that are out of stock
     */
    @Query("SELECT c FROM CartItem c WHERE c.user.id = :userId AND " +
           "(c.product.stockQuantity < c.quantity OR c.product.stockQuantity = 0)")
    List<CartItem> findCartItemsWithInsufficientStockByUserId(@Param("userId") Long userId);
    
    /**
     * Find cart items with inactive products
     */
    @Query("SELECT c FROM CartItem c WHERE c.product.isActive = false")
    List<CartItem> findCartItemsWithInactiveProducts();
    
    /**
     * Find cart items for user with inactive products
     */
    @Query("SELECT c FROM CartItem c WHERE c.user.id = :userId AND c.product.isActive = false")
    List<CartItem> findCartItemsWithInactiveProductsByUserId(@Param("userId") Long userId);
    
    /**
     * Find old cart items (older than specified date)
     */
    @Query("SELECT c FROM CartItem c WHERE c.addedDate < :cutoffDate")
    List<CartItem> findOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete old cart items (older than specified date)
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.addedDate < :cutoffDate")
    void deleteOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find cart items by product
     */
    List<CartItem> findByProduct(Product product);
    
    /**
     * Find cart items by product ID
     */
    List<CartItem> findByProductId(Long productId);
    
    /**
     * Update cart item quantity
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.quantity = :quantity, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.user.id = :userId AND c.product.id = :productId")
    int updateQuantity(@Param("userId") Long userId, 
                      @Param("productId") Long productId, 
                      @Param("quantity") Integer quantity);
    
    /**
     * Update unit price for cart items when product price changes
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.unitPrice = :newPrice, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.product.id = :productId")
    int updateUnitPriceByProductId(@Param("productId") Long productId, 
                                  @Param("newPrice") BigDecimal newPrice);
}