package com.dacsanviet.repository;

// Removed Role import - using admin boolean instead
import com.dacsanviet.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    

    
    /**
     * Find users by admin status
     */
    List<User> findByAdmin(Boolean admin);
    
    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find users by admin and active status
     */
    List<User> findByAdminAndIsActive(Boolean admin, Boolean isActive);
    
    /**
     * Find users created within date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Search users by username, email, or full name
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find users with orders
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o")
    List<User> findUsersWithOrders();
    
    /**
     * Count users by admin status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.admin = :admin")
    Long countByAdmin(@Param("admin") Boolean admin);
    
    /**
     * Find users who registered in the last N days
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :sinceDate ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find users with cart items
     */
    @Query("SELECT DISTINCT u FROM User u WHERE SIZE(u.cartItems) > 0")
    List<User> findUsersWithCartItems();
    
    /**
     * Count new customers in date range
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countNewCustomers(@Param("startDate") LocalDateTime startDate, 
                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Search customers with advanced filters
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:admin IS NULL OR u.admin = :admin) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:registeredAfter IS NULL OR u.createdAt >= :registeredAfter) AND " +
           "(:registeredBefore IS NULL OR u.createdAt <= :registeredBefore)")
    Page<User> searchCustomersAdvanced(@Param("searchTerm") String searchTerm,
                                      @Param("admin") Boolean admin,
                                      @Param("isActive") Boolean isActive,
                                      @Param("registeredAfter") LocalDateTime registeredAfter,
                                      @Param("registeredBefore") LocalDateTime registeredBefore,
                                      Pageable pageable);
    
    /**
     * Count users by created at between
     */
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}