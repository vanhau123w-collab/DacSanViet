package com.dacsanviet.repository;

import com.dacsanviet.model.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NewsCategory entity operations
 * Supports CRUD operations, search, and filtering for news categories
 */
@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    
    // Basic finders
    /**
     * Find category by name
     */
    Optional<NewsCategory> findByName(String name);
    
    /**
     * Find category by slug
     */
    Optional<NewsCategory> findBySlug(String slug);
    
    /**
     * Check if category name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if category slug exists
     */
    boolean existsBySlug(String slug);
    
    // Active categories
    /**
     * Find all active categories ordered by sort order then name
     */
    List<NewsCategory> findByIsActiveTrueOrderBySortOrderAscNameAsc();
    
    /**
     * Find all active categories ordered by name
     */
    List<NewsCategory> findByIsActiveTrueOrderByNameAsc();
    
    /**
     * Find categories by active status
     */
    List<NewsCategory> findByIsActiveOrderByNameAsc(Boolean isActive);
    
    /**
     * Find categories by active status with pagination
     */
    Page<NewsCategory> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Search functionality
    /**
     * Search categories by name or description
     */
    @Query("SELECT c FROM NewsCategory c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<NewsCategory> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search active categories by name or description
     */
    @Query("SELECT c FROM NewsCategory c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<NewsCategory> searchActiveCategories(@Param("searchTerm") String searchTerm);
    
    // Categories with articles
    /**
     * Find categories that have published articles
     */
    @Query("SELECT DISTINCT c FROM NewsCategory c JOIN c.articles a WHERE a.status = 'PUBLISHED' AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<NewsCategory> findCategoriesWithPublishedArticles();
    
    /**
     * Find categories with article count (published articles only)
     */
    @Query("SELECT c, COUNT(a) as articleCount FROM NewsCategory c " +
           "LEFT JOIN c.articles a ON a.status = 'PUBLISHED' " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<Object[]> findActiveCategoriesWithPublishedArticleCount();
    
    /**
     * Find categories with article count (all articles)
     */
    @Query("SELECT c, COUNT(a) as articleCount FROM NewsCategory c " +
           "LEFT JOIN c.articles a " +
           "GROUP BY c " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<Object[]> findCategoriesWithArticleCount();
    
    /**
     * Count published articles in category
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.category.id = :categoryId AND a.status = 'PUBLISHED'")
    Long countPublishedArticlesInCategory(@Param("categoryId") Long categoryId);
    
    /**
     * Count all articles in category
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.category.id = :categoryId")
    Long countArticlesInCategory(@Param("categoryId") Long categoryId);
    
    // Categories ordered by article count
    /**
     * Find active categories ordered by published article count (descending)
     */
    @Query("SELECT c FROM NewsCategory c " +
           "LEFT JOIN c.articles a ON a.status = 'PUBLISHED' " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY COUNT(a) DESC, c.name ASC")
    List<NewsCategory> findActiveCategoriesOrderedByPublishedArticleCount();
    
    /**
     * Find categories ordered by total article count (descending)
     */
    @Query("SELECT c FROM NewsCategory c " +
           "LEFT JOIN c.articles a " +
           "GROUP BY c " +
           "ORDER BY COUNT(a) DESC, c.name ASC")
    List<NewsCategory> findCategoriesOrderedByArticleCount();
    
    // Empty categories
    /**
     * Find categories with no articles
     */
    @Query("SELECT c FROM NewsCategory c WHERE c.id NOT IN " +
           "(SELECT DISTINCT a.category.id FROM NewsArticle a WHERE a.category IS NOT NULL)")
    List<NewsCategory> findEmptyCategories();
    
    /**
     * Find categories with no published articles
     */
    @Query("SELECT c FROM NewsCategory c WHERE c.id NOT IN " +
           "(SELECT DISTINCT a.category.id FROM NewsArticle a WHERE a.category IS NOT NULL AND a.status = 'PUBLISHED')")
    List<NewsCategory> findCategoriesWithoutPublishedArticles();
    
    // Sort order management
    /**
     * Find categories with sort order greater than specified value
     */
    @Query("SELECT c FROM NewsCategory c WHERE c.sortOrder > :sortOrder ORDER BY c.sortOrder ASC")
    List<NewsCategory> findCategoriesWithSortOrderGreaterThan(@Param("sortOrder") Integer sortOrder);
    
    /**
     * Find maximum sort order
     */
    @Query("SELECT MAX(c.sortOrder) FROM NewsCategory c")
    Integer findMaxSortOrder();
    
    /**
     * Find categories ordered by sort order
     */
    List<NewsCategory> findAllByOrderBySortOrderAscNameAsc();
    
    // Slug generation support
    /**
     * Find categories with slug starting with prefix (for slug uniqueness)
     */
    @Query("SELECT c FROM NewsCategory c WHERE c.slug LIKE CONCAT(:slugPrefix, '%')")
    List<NewsCategory> findBySlugStartingWith(@Param("slugPrefix") String slugPrefix);
    
    /**
     * Count categories with slug starting with prefix
     */
    @Query("SELECT COUNT(c) FROM NewsCategory c WHERE c.slug LIKE CONCAT(:slugPrefix, '%')")
    Long countBySlugStartingWith(@Param("slugPrefix") String slugPrefix);
    
    // Advanced filtering
    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT c FROM NewsCategory c WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<NewsCategory> searchCategoriesAdvanced(@Param("searchTerm") String searchTerm,
                                              @Param("isActive") Boolean isActive,
                                              Pageable pageable);
}