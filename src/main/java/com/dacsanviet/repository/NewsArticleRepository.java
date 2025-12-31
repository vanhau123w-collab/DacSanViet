package com.dacsanviet.repository;

import com.dacsanviet.model.NewsArticle;
import com.dacsanviet.model.NewsCategory;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NewsArticle entity operations
 * Supports CRUD operations, search, filtering, and analytics
 */
@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    // Basic finders
    /**
     * Find article by slug
     */
    Optional<NewsArticle> findBySlug(String slug);
    
    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
    
    /**
     * Find articles by status
     */
    Page<NewsArticle> findByStatus(NewsStatus status, Pageable pageable);
    
    /**
     * Find articles by author
     */
    Page<NewsArticle> findByAuthor(User author, Pageable pageable);
    
    /**
     * Find articles by category
     */
    Page<NewsArticle> findByCategory(NewsCategory category, Pageable pageable);
    
    /**
     * Find articles by category id
     */
    Page<NewsArticle> findByCategoryId(Long categoryId, Pageable pageable);
    
    // Published articles queries (for public display)
    /**
     * Find all published articles ordered by published date desc
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<NewsArticle> findPublishedArticles(Pageable pageable);
    
    /**
     * Find published articles by category
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.category.id = :categoryId ORDER BY a.publishedAt DESC")
    Page<NewsArticle> findPublishedArticlesByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Find published articles by category slug
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.category.slug = :categorySlug ORDER BY a.publishedAt DESC")
    Page<NewsArticle> findPublishedArticlesByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);
    
    /**
     * Find featured articles (published only)
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.isFeatured = true ORDER BY a.publishedAt DESC")
    List<NewsArticle> findFeaturedArticles(Pageable pageable);
    
    /**
     * Find recent published articles
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    List<NewsArticle> findRecentPublishedArticles(Pageable pageable);
    
    // Search functionality
    /**
     * Search articles by title and content (for admin)
     */
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<NewsArticle> searchArticles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search published articles by title and content (for public)
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    Page<NewsArticle> searchPublishedArticles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
           "(:authorId IS NULL OR a.author.id = :authorId) AND " +
           "(:isFeatured IS NULL OR a.isFeatured = :isFeatured) AND " +
           "(:publishedAfter IS NULL OR a.publishedAt >= :publishedAfter) AND " +
           "(:publishedBefore IS NULL OR a.publishedAt <= :publishedBefore)")
    Page<NewsArticle> searchArticlesAdvanced(@Param("searchTerm") String searchTerm,
                                           @Param("status") NewsStatus status,
                                           @Param("categoryId") Long categoryId,
                                           @Param("authorId") Long authorId,
                                           @Param("isFeatured") Boolean isFeatured,
                                           @Param("publishedAfter") LocalDateTime publishedAfter,
                                           @Param("publishedBefore") LocalDateTime publishedBefore,
                                           Pageable pageable);
    
    // Date range filtering
    /**
     * Find articles published within date range
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.publishedAt BETWEEN :startDate AND :endDate ORDER BY a.publishedAt DESC")
    Page<NewsArticle> findPublishedArticlesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       Pageable pageable);
    
    /**
     * Find articles created within date range
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<NewsArticle> findArticlesCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    // Analytics and statistics
    /**
     * Find most viewed articles
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' ORDER BY a.viewCount DESC")
    List<NewsArticle> findMostViewedArticles(Pageable pageable);
    
    /**
     * Count articles by status
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.status = :status")
    Long countByStatus(@Param("status") NewsStatus status);
    
    /**
     * Count articles by category
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.category.id = :categoryId")
    Long countByCategory(@Param("categoryId") Long categoryId);
    
    /**
     * Count published articles by category
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.category.id = :categoryId")
    Long countPublishedByCategory(@Param("categoryId") Long categoryId);
    
    /**
     * Get article statistics by status
     */
    @Query("SELECT a.status, COUNT(a) FROM NewsArticle a GROUP BY a.status")
    List<Object[]> getArticleStatsByStatus();
    
    /**
     * Get article statistics by category
     */
    @Query("SELECT c.name, COUNT(a) FROM NewsArticle a JOIN a.category c WHERE a.status = 'PUBLISHED' GROUP BY c.name ORDER BY COUNT(a) DESC")
    List<Object[]> getPublishedArticleStatsByCategory();
    
    /**
     * Get view statistics by month
     */
    @Query("SELECT YEAR(a.publishedAt), MONTH(a.publishedAt), SUM(a.viewCount) " +
           "FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND a.publishedAt >= :sinceDate " +
           "GROUP BY YEAR(a.publishedAt), MONTH(a.publishedAt) " +
           "ORDER BY YEAR(a.publishedAt) DESC, MONTH(a.publishedAt) DESC")
    List<Object[]> getViewStatsByMonth(@Param("sinceDate") LocalDateTime sinceDate);
    
    // View count operations
    /**
     * Increment view count for an article
     */
    @Modifying
    @Query("UPDATE NewsArticle a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * Get total view count for all published articles
     */
    @Query("SELECT SUM(a.viewCount) FROM NewsArticle a WHERE a.status = 'PUBLISHED'")
    Long getTotalViewCount();
    
    // Category-related queries
    /**
     * Find articles without category
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.category IS NULL")
    List<NewsArticle> findArticlesWithoutCategory();
    
    /**
     * Find categories with article count
     */
    @Query("SELECT c, COUNT(a) as articleCount FROM NewsCategory c " +
           "LEFT JOIN c.articles a ON a.status = 'PUBLISHED' " +
           "WHERE c.isActive = true " +
           "GROUP BY c " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findCategoriesWithPublishedArticleCount();
    
    // Author-related queries
    /**
     * Find articles by author with status
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.author.id = :authorId AND a.status = :status ORDER BY a.createdAt DESC")
    List<NewsArticle> findByAuthorAndStatus(@Param("authorId") Long authorId, @Param("status") NewsStatus status);
    
    /**
     * Count articles by author
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.author.id = :authorId")
    Long countByAuthor(@Param("authorId") Long authorId);
    
    /**
     * Find top authors by article count
     */
    @Query("SELECT a.author, COUNT(a) as articleCount FROM NewsArticle a " +
           "WHERE a.status = 'PUBLISHED' " +
           "GROUP BY a.author " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopAuthorsByArticleCount(Pageable pageable);
    
    // Slug generation support
    /**
     * Find articles with slug starting with prefix (for slug uniqueness)
     */
    @Query("SELECT a FROM NewsArticle a WHERE a.slug LIKE CONCAT(:slugPrefix, '%')")
    List<NewsArticle> findBySlugStartingWith(@Param("slugPrefix") String slugPrefix);
    
    /**
     * Count articles with slug starting with prefix
     */
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.slug LIKE CONCAT(:slugPrefix, '%')")
    Long countBySlugStartingWith(@Param("slugPrefix") String slugPrefix);
}