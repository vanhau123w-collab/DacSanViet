package com.dacsanviet.service;

import com.dacsanviet.dto.NewsCommentDto;
import com.dacsanviet.model.CommentStatus;
import com.dacsanviet.model.NewsArticle;
import com.dacsanviet.model.NewsComment;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.NewsArticleRepository;
import com.dacsanviet.repository.NewsCommentRepository;
import com.dacsanviet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing news comments
 * Handles CRUD operations, moderation, and business logic for comments
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsCommentService {
    
    private final NewsCommentRepository newsCommentRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    
    // CRUD Operations
    
    /**
     * Create a new user comment
     */
    public NewsCommentDto createUserComment(NewsCommentDto commentDto) {
        log.info("Creating new user comment for article id: {} by user id: {}", 
                commentDto.getArticleId(), commentDto.getUserId());
        
        // Validate article exists
        NewsArticle article = newsArticleRepository.findById(commentDto.getArticleId())
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + commentDto.getArticleId()));
        
        // Validate user exists
        User user = userRepository.findById(commentDto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + commentDto.getUserId()));
        
        // Validate parent comment if this is a reply
        NewsComment parent = null;
        if (commentDto.getParentId() != null) {
            parent = newsCommentRepository.findById(commentDto.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id: " + commentDto.getParentId()));
            
            // Ensure parent comment belongs to the same article
            if (!parent.getArticle().getId().equals(commentDto.getArticleId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified article");
            }
        }
        
        // Create comment entity
        NewsComment comment = new NewsComment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(commentDto.getContent());
        comment.setParent(parent);
        comment.setStatus(CommentStatus.PENDING); // All comments start as pending
        
        NewsComment savedComment = newsCommentRepository.save(comment);
        log.info("Created user comment with id: {}", savedComment.getId());
        
        return convertToDto(savedComment);
    }
    
    /**
     * Create a new guest comment
     */
    public NewsCommentDto createGuestComment(NewsCommentDto commentDto) {
        log.info("Creating new guest comment for article id: {} by guest: {}", 
                commentDto.getArticleId(), commentDto.getGuestName());
        
        // Validate article exists
        NewsArticle article = newsArticleRepository.findById(commentDto.getArticleId())
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + commentDto.getArticleId()));
        
        // Validate parent comment if this is a reply
        NewsComment parent = null;
        if (commentDto.getParentId() != null) {
            parent = newsCommentRepository.findById(commentDto.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id: " + commentDto.getParentId()));
            
            // Ensure parent comment belongs to the same article
            if (!parent.getArticle().getId().equals(commentDto.getArticleId())) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified article");
            }
        }
        
        // Create comment entity
        NewsComment comment = new NewsComment();
        comment.setArticle(article);
        comment.setGuestName(commentDto.getGuestName());
        comment.setGuestEmail(commentDto.getGuestEmail());
        comment.setContent(commentDto.getContent());
        comment.setParent(parent);
        comment.setStatus(CommentStatus.PENDING); // All comments start as pending
        
        NewsComment savedComment = newsCommentRepository.save(comment);
        log.info("Created guest comment with id: {}", savedComment.getId());
        
        return convertToDto(savedComment);
    }
    
    /**
     * Update a comment (only content can be updated)
     */
    public NewsCommentDto updateComment(Long id, String newContent) {
        log.info("Updating comment with id: {}", id);
        
        NewsComment comment = newsCommentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        
        comment.setContent(newContent);
        // Reset status to pending when content is updated
        comment.setStatus(CommentStatus.PENDING);
        
        NewsComment savedComment = newsCommentRepository.save(comment);
        log.info("Updated comment with id: {}", savedComment.getId());
        
        return convertToDto(savedComment);
    }
    
    /**
     * Delete a comment (hard delete)
     */
    public void deleteComment(Long id) {
        log.info("Deleting comment with id: {}", id);
        
        NewsComment comment = newsCommentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        
        newsCommentRepository.delete(comment);
        log.info("Deleted comment with id: {}", id);
    }
    
    /**
     * Find comment by ID
     */
    @Transactional(readOnly = true)
    public Optional<NewsCommentDto> findById(Long id) {
        return newsCommentRepository.findById(id)
            .map(this::convertToDto);
    }
    
    // Query Operations
    
    /**
     * Find approved comments by article (for public display)
     */
    @Transactional(readOnly = true)
    public List<NewsCommentDto> findApprovedCommentsByArticle(Long articleId) {
        List<NewsComment> comments = newsCommentRepository.findApprovedCommentsByArticle(articleId);
        return buildCommentTree(comments);
    }
    
    /**
     * Find approved comments by article with pagination
     */
    @Transactional(readOnly = true)
    public Page<NewsCommentDto> findApprovedCommentsByArticle(Long articleId, Pageable pageable) {
        return newsCommentRepository.findApprovedCommentsByArticle(articleId, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find all comments by article (for admin)
     */
    @Transactional(readOnly = true)
    public List<NewsCommentDto> findAllCommentsByArticle(Long articleId) {
        List<NewsComment> comments = newsCommentRepository.findByArticleId(articleId);
        return buildCommentTree(comments);
    }
    
    /**
     * Find comments by user
     */
    @Transactional(readOnly = true)
    public Page<NewsCommentDto> findCommentsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        return newsCommentRepository.findByUser(user, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find comments by status
     */
    @Transactional(readOnly = true)
    public Page<NewsCommentDto> findCommentsByStatus(CommentStatus status, Pageable pageable) {
        return newsCommentRepository.findByStatus(status, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Search comments by content
     */
    @Transactional(readOnly = true)
    public Page<NewsCommentDto> searchComments(String keyword, Pageable pageable) {
        return newsCommentRepository.searchCommentsByContent(keyword, pageable)
            .map(this::convertToDto);
    }
    
    // Moderation Operations
    
    /**
     * Find pending comments for moderation
     */
    @Transactional(readOnly = true)
    public Page<NewsCommentDto> findPendingComments(Pageable pageable) {
        return newsCommentRepository.findPendingComments(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Approve a comment
     */
    public void approveComment(Long id) {
        log.info("Approving comment with id: {}", id);
        
        NewsComment comment = newsCommentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        
        comment.setStatus(CommentStatus.APPROVED);
        newsCommentRepository.save(comment);
        
        log.info("Approved comment with id: {}", id);
    }
    
    /**
     * Reject a comment
     */
    public void rejectComment(Long id) {
        log.info("Rejecting comment with id: {}", id);
        
        NewsComment comment = newsCommentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        
        comment.setStatus(CommentStatus.REJECTED);
        newsCommentRepository.save(comment);
        
        log.info("Rejected comment with id: {}", id);
    }
    
    /**
     * Set comment status back to pending
     */
    public void setPendingComment(Long id) {
        log.info("Setting comment to pending with id: {}", id);
        
        NewsComment comment = newsCommentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        
        comment.setStatus(CommentStatus.PENDING);
        newsCommentRepository.save(comment);
        
        log.info("Set comment to pending with id: {}", id);
    }
    
    /**
     * Bulk approve comments
     */
    public void bulkApproveComments(List<Long> commentIds) {
        log.info("Bulk approving {} comments", commentIds.size());
        
        for (Long id : commentIds) {
            try {
                approveComment(id);
            } catch (Exception e) {
                log.error("Failed to approve comment with id: {}", id, e);
            }
        }
        
        log.info("Completed bulk approval of {} comments", commentIds.size());
    }
    
    /**
     * Bulk reject comments
     */
    public void bulkRejectComments(List<Long> commentIds) {
        log.info("Bulk rejecting {} comments", commentIds.size());
        
        for (Long id : commentIds) {
            try {
                rejectComment(id);
            } catch (Exception e) {
                log.error("Failed to reject comment with id: {}", id, e);
            }
        }
        
        log.info("Completed bulk rejection of {} comments", commentIds.size());
    }
    
    // Analytics and Statistics
    
    /**
     * Count comments by article
     */
    @Transactional(readOnly = true)
    public Long countCommentsByArticle(Long articleId) {
        return newsCommentRepository.countByArticle(articleId);
    }
    
    /**
     * Count approved comments by article
     */
    @Transactional(readOnly = true)
    public Long countApprovedCommentsByArticle(Long articleId) {
        return newsCommentRepository.countApprovedByArticle(articleId);
    }
    
    /**
     * Get comment statistics by status
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCommentStatsByStatus() {
        List<Object[]> stats = newsCommentRepository.getCommentStatsByStatus();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            CommentStatus status = (CommentStatus) stat[0];
            Long count = (Long) stat[1];
            result.put(status.name(), count);
        }
        
        return result;
    }
    
    /**
     * Get comment statistics by article
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCommentStatsByArticle() {
        List<Object[]> stats = newsCommentRepository.getCommentStatsByArticle();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            String articleTitle = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(articleTitle, count);
        }
        
        return result;
    }
    
    /**
     * Find most active commenters
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findMostActiveCommenters(int limit) {
        List<Object[]> results = newsCommentRepository.findMostActiveCommenters(
            org.springframework.data.domain.PageRequest.of(0, limit));
        
        return results.stream()
            .map(result -> {
                User user = (User) result[0];
                Long commentCount = (Long) result[1];
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("userName", user.getFullName() != null ? user.getFullName() : user.getUsername());
                map.put("commentCount", commentCount);
                return map;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Find articles with most comments
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findArticlesWithMostComments(int limit) {
        List<Object[]> results = newsCommentRepository.findArticlesWithMostComments(
            org.springframework.data.domain.PageRequest.of(0, limit));
        
        return results.stream()
            .map(result -> {
                NewsArticle article = (NewsArticle) result[0];
                Long commentCount = (Long) result[1];
                Map<String, Object> map = new HashMap<>();
                map.put("articleId", article.getId());
                map.put("articleTitle", article.getTitle());
                map.put("articleSlug", article.getSlug());
                map.put("commentCount", commentCount);
                return map;
            })
            .collect(Collectors.toList());
    }
    
    // Helper Methods
    
    /**
     * Build comment tree structure with replies
     */
    private List<NewsCommentDto> buildCommentTree(List<NewsComment> comments) {
        Map<Long, NewsCommentDto> commentMap = new HashMap<>();
        List<NewsCommentDto> rootComments = comments.stream()
            .map(comment -> {
                NewsCommentDto dto = convertToDto(comment);
                commentMap.put(dto.getId(), dto);
                return dto;
            })
            .collect(Collectors.toList());
        
        // Build parent-child relationships
        for (NewsCommentDto comment : rootComments) {
            if (comment.getParentId() != null) {
                NewsCommentDto parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }
        
        // Return only top-level comments (replies are nested)
        return rootComments.stream()
            .filter(comment -> comment.getParentId() == null)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert NewsComment entity to DTO
     */
    private NewsCommentDto convertToDto(NewsComment comment) {
        NewsCommentDto dto = new NewsCommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        
        // Set article info
        if (comment.getArticle() != null) {
            dto.setArticleId(comment.getArticle().getId());
            dto.setArticleTitle(comment.getArticle().getTitle());
        }
        
        // Set user info
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getFullName() != null ? 
                comment.getUser().getFullName() : comment.getUser().getUsername());
        } else {
            // Guest comment
            dto.setGuestName(comment.getGuestName());
            dto.setGuestEmail(comment.getGuestEmail());
        }
        
        // Set parent info
        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
            dto.setParentAuthorName(comment.getParent().getAuthorName());
        }
        
        return dto;
    }
}