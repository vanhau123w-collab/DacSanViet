package com.dacsanviet.repository;

import com.dacsanviet.model.CommentStatus;
import com.dacsanviet.model.NewsArticle;
import com.dacsanviet.model.NewsComment;
import com.dacsanviet.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for NewsComment entity operations
 * Supports CRUD operations, search, filtering, and moderation for news comments
 */
@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {
    
    // Basic finders
    /**
     * Find comments by article
     */
    List<NewsComment> findByArticle(NewsArticle article);
    
    /**
     * Find comments by article id
     */
    List<NewsComment> findByArticleId(Long articleId);
    
    /**
     * Find comments by article with pagination
     */
    Page<NewsComment> findByArticle(NewsArticle article, Pageable pageable);
    
    /**
     * Find comments by article id with pagination
     */
    Page<NewsComment> findByArticleId(Long articleId, Pageable pageable);
    
    /**
     * Find comments by user
     */
    List<NewsComment> findByUser(User user);
    
    /**
     * Find comments by user with pagination
     */
    Page<NewsComment> findByUser(User user, Pageable pageable);
    
    /**
     * Find comments by status
     */
    Page<NewsComment> findByStatus(CommentStatus status, Pageable pageable);
    
    // Approved comments (for public display)
    /**
     * Find approved comments by article ordered by creation date
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.status = 'APPROVED' ORDER BY c.createdAt ASC")
    List<NewsComment> findApprovedCommentsByArticle(@Param("articleId") Long articleId);
    
    /**
     * Find approved comments by article with pagination
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.status = 'APPROVED' ORDER BY c.createdAt ASC")
    Page<NewsComment> findApprovedCommentsByArticle(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * Find approved top-level comments (no parent) by article
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.status = 'APPROVED' AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<NewsComment> findApprovedTopLevelCommentsByArticle(@Param("articleId") Long articleId);
    
    /**
     * Find approved replies to a comment
     */
    @Query("SELECT c FROM NewsComment c WHERE c.parent.id = :parentId AND c.status = 'APPROVED' ORDER BY c.createdAt ASC")
    List<NewsComment> findApprovedRepliesByParent(@Param("parentId") Long parentId);
    
    // Comment threading
    /**
     * Find top-level comments (no parent) by article
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    List<NewsComment> findTopLevelCommentsByArticle(@Param("articleId") Long articleId);
    
    /**
     * Find top-level comments by article with pagination
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<NewsComment> findTopLevelCommentsByArticle(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * Find replies to a comment
     */
    @Query("SELECT c FROM NewsComment c WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<NewsComment> findRepliesByParent(@Param("parentId") Long parentId);
    
    /**
     * Find all replies in a thread (recursive)
     */
    @Query("SELECT c FROM NewsComment c WHERE c.parent.id = :parentId OR c.parent.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<NewsComment> findAllRepliesInThread(@Param("parentId") Long parentId);
    
    // Comment moderation
    /**
     * Find pending comments for moderation
     */
    @Query("SELECT c FROM NewsComment c WHERE c.status = 'PENDING' ORDER BY c.createdAt ASC")
    List<NewsComment> findPendingComments();
    
    /**
     * Find pending comments with pagination
     */
    @Query("SELECT c FROM NewsComment c WHERE c.status = 'PENDING' ORDER BY c.createdAt ASC")
    Page<NewsComment> findPendingComments(Pageable pageable);
    
    /**
     * Find comments by status and article
     */
    @Query("SELECT c FROM NewsComment c WHERE c.article.id = :articleId AND c.status = :status ORDER BY c.createdAt DESC")
    List<NewsComment> findByArticleAndStatus(@Param("articleId") Long articleId, @Param("status") CommentStatus status);
    
    /**
     * Find recent comments for moderation (all statuses)
     */
    @Query("SELECT c FROM NewsComment c ORDER BY c.createdAt DESC")
    Page<NewsComment> findRecentComments(Pageable pageable);
    
    // Search functionality
    /**
     * Search comments by content
     */
    @Query("SELECT c FROM NewsComment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<NewsComment> searchCommentsByContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search approved comments by content
     */
    @Query("SELECT c FROM NewsComment c WHERE c.status = 'APPROVED' AND LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<NewsComment> searchApprovedCommentsByContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search comments by author name (user or guest)
     */
    @Query("SELECT c FROM NewsComment c WHERE " +
           "LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR " +
           "LOWER(c.guestName) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<NewsComment> searchCommentsByAuthorName(@Param("authorName") String authorName, Pageable pageable);
    
    // Statistics and analytics
    /**
     * Count comments by article
     */
    @Query("SELECT COUNT(c) FROM NewsComment c WHERE c.article.id = :articleId")
    Long countByArticle(@Param("articleId") Long articleId);
    
    /**
     * Count approved comments by article
     */
    @Query("SELECT COUNT(c) FROM NewsComment c WHERE c.article.id = :articleId AND c.status = 'APPROVED'")
    Long countApprovedByArticle(@Param("articleId") Long articleId);
    
    /**
     * Count comments by status
     */
    @Query("SELECT COUNT(c) FROM NewsComment c WHERE c.status = :status")
    Long countByStatus(@Param("status") CommentStatus status);
    
    /**
     * Count comments by user
     */
    @Query("SELECT COUNT(c) FROM NewsComment c WHERE c.user.id = :userId")
    Long countByUser(@Param("userId") Long userId);
    
    /**
     * Get comment statistics by status
     */
    @Query("SELECT c.status, COUNT(c) FROM NewsComment c GROUP BY c.status")
    List<Object[]> getCommentStatsByStatus();
    
    /**
     * Get comment statistics by article
     */
    @Query("SELECT a.title, COUNT(c) FROM NewsComment c JOIN c.article a WHERE c.status = 'APPROVED' GROUP BY a.title ORDER BY COUNT(c) DESC")
    List<Object[]> getCommentStatsByArticle();
    
    /**
     * Get comment statistics by month
     */
    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c) " +
           "FROM NewsComment c WHERE c.createdAt >= :sinceDate " +
           "GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) " +
           "ORDER BY YEAR(c.createdAt) DESC, MONTH(c.createdAt) DESC")
    List<Object[]> getCommentStatsByMonth(@Param("sinceDate") LocalDateTime sinceDate);
    
    // Date range queries
    /**
     * Find comments created within date range
     */
    @Query("SELECT c FROM NewsComment c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<NewsComment> findCommentsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find comments created within date range with pagination
     */
    @Query("SELECT c FROM NewsComment c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    Page<NewsComment> findCommentsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);
    
    // Guest vs User comments
    /**
     * Find guest comments (user is null)
     */
    @Query("SELECT c FROM NewsComment c WHERE c.user IS NULL ORDER BY c.createdAt DESC")
    Page<NewsComment> findGuestComments(Pageable pageable);
    
    /**
     * Find user comments (user is not null)
     */
    @Query("SELECT c FROM NewsComment c WHERE c.user IS NOT NULL ORDER BY c.createdAt DESC")
    Page<NewsComment> findUserComments(Pageable pageable);
    
    /**
     * Find guest comments by email
     */
    @Query("SELECT c FROM NewsComment c WHERE c.guestEmail = :email ORDER BY c.createdAt DESC")
    List<NewsComment> findGuestCommentsByEmail(@Param("email") String email);
    
    // Advanced filtering
    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT c FROM NewsComment c WHERE " +
           "(:searchTerm IS NULL OR LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:articleId IS NULL OR c.article.id = :articleId) AND " +
           "(:userId IS NULL OR c.user.id = :userId) AND " +
           "(:isGuest IS NULL OR (:isGuest = true AND c.user IS NULL) OR (:isGuest = false AND c.user IS NOT NULL)) AND " +
           "(:createdAfter IS NULL OR c.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR c.createdAt <= :createdBefore)")
    Page<NewsComment> searchCommentsAdvanced(@Param("searchTerm") String searchTerm,
                                           @Param("status") CommentStatus status,
                                           @Param("articleId") Long articleId,
                                           @Param("userId") Long userId,
                                           @Param("isGuest") Boolean isGuest,
                                           @Param("createdAfter") LocalDateTime createdAfter,
                                           @Param("createdBefore") LocalDateTime createdBefore,
                                           Pageable pageable);
    
    // Most active commenters
    /**
     * Find most active users by comment count
     */
    @Query("SELECT c.user, COUNT(c) as commentCount FROM NewsComment c " +
           "WHERE c.user IS NOT NULL AND c.status = 'APPROVED' " +
           "GROUP BY c.user " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> findMostActiveCommenters(Pageable pageable);
    
    /**
     * Find articles with most comments
     */
    @Query("SELECT c.article, COUNT(c) as commentCount FROM NewsComment c " +
           "WHERE c.status = 'APPROVED' " +
           "GROUP BY c.article " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> findArticlesWithMostComments(Pageable pageable);
}