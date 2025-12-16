package com.specialtyfood.repository;

import com.specialtyfood.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);
    
    /**
     * Check if category name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active categories
     */
    List<Category> findByIsActiveTrueOrderByName();
    
    /**
     * Find categories by active status
     */
    List<Category> findByIsActiveOrderByName(Boolean isActive);
    
    /**
     * Search categories by name or description
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find categories with products
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.isActive = true")
    List<Category> findCategoriesWithActiveProducts();
    
    /**
     * Count products in category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Long countActiveProductsInCategory(@Param("categoryId") Long categoryId);
    
    /**
     * Find categories with product count
     */
    @Query("SELECT c, COUNT(p) as productCount FROM Category c " +
           "LEFT JOIN c.products p ON p.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY c.name")
    List<Object[]> findCategoriesWithProductCount();
    
    /**
     * Find categories ordered by product count
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN c.products p ON p.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY COUNT(p) DESC")
    List<Category> findCategoriesOrderedByProductCount();
    
    /**
     * Find empty categories (no products)
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN " +
           "(SELECT DISTINCT p.category.id FROM Product p WHERE p.isActive = true)")
    List<Category> findEmptyCategories();
}