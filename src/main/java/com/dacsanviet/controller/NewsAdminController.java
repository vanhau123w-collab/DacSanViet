package com.dacsanviet.controller;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin REST API Controller for News Management
 * Provides CRUD operations, search, filtering, and analytics for news articles
 * Requires ADMIN or STAFF role for access
 */
@RestController
@RequestMapping("/api/admin/news")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Slf4j
public class NewsAdminController {
    
    private final NewsService newsService;
    
    @Value("${app.upload.news-images:uploads/news}")
    private String uploadPath;
    
    /**
     * Test endpoint to check if API is working
     */
    @PostMapping("/test")
    public ResponseEntity<?> testEndpoint(@RequestParam("test") String test) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test successful");
        response.put("received", test);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint for multipart form data
     */
    @PostMapping("/test-multipart")
    public ResponseEntity<?> testMultipartEndpoint(
            @RequestParam("title") String title,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Multipart test successful");
        response.put("title", title);
        response.put("hasFile", file != null && !file.isEmpty());
        if (file != null) {
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Debug endpoint to check if update endpoint is accessible
     */
    @PostMapping("/{id}/update-debug")
    public ResponseEntity<?> debugUpdateEndpoint(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Update endpoint is accessible");
        response.put("articleId", id);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Simple update without file upload for testing
     */
    @PostMapping("/{id}/update-simple")
    public ResponseEntity<?> updateArticleSimple(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("categoryId") Long categoryId) {
        
        try {
            log.info("Simple update for article: {}", id);
            
            // Get existing article
            Optional<NewsArticleDto> existingArticleOpt = newsService.findById(id);
            if (existingArticleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Không tìm thấy bài viết với ID: " + id));
            }
            
            NewsArticleDto existingArticle = existingArticleOpt.get();
            
            // Create updated article DTO
            NewsArticleDto articleDto = new NewsArticleDto();
            articleDto.setId(id);
            articleDto.setTitle(title);
            articleDto.setContent(content);
            articleDto.setCategoryId(categoryId);
            articleDto.setAuthorId(existingArticle.getAuthorId());
            articleDto.setStatus(NewsStatus.DRAFT);
            articleDto.setIsFeatured(false);
            
            // Preserve existing image
            articleDto.setFeaturedImage(existingArticle.getFeaturedImage());
            
            // Update article
            NewsArticleDto updatedArticle = newsService.updateArticle(id, articleDto);
            
            log.info("Simple update successful for article: {}", id);
            return ResponseEntity.ok(updatedArticle);
            
        } catch (Exception e) {
            log.error("Error in simple update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật: " + e.getMessage()));
        }
    }
    
    /**
     * Catch all requests to debug routing issues
     */
    @RequestMapping(value = "/{id}/update", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> catchAllUpdate(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("=== CATCH-ALL UPDATE ENDPOINT ===");
        log.info("Method: {}", request.getMethod());
        log.info("Content-Type: {}", request.getContentType());
        log.info("Article ID: {}", id);
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Parameters: {}", request.getParameterMap().keySet());
        log.info("================================");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Endpoint reached but not processed correctly");
        response.put("method", request.getMethod());
        response.put("contentType", request.getContentType());
        response.put("articleId", id);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Get all articles with pagination, sorting, and filtering
     * 
     * @param page Page number (0-based)
     * @param size Page size (default 10)
     * @param sort Sort field (default: createdAt)
     * @param direction Sort direction (default: desc)
     * @param status Filter by status
     * @param categoryId Filter by category
     * @param search Search term for title/content
     * @param featured Filter by featured status
     * @return Paginated list of articles
     */
    @GetMapping
    public ResponseEntity<?> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured) {
        
        try {
            log.info("Getting articles - page: {}, size: {}, status: {}, search: {}", 
                    page, size, status, search);
            
            // Create sort object
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sortObj = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // Parse status if provided
            NewsStatus newsStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    newsStatus = NewsStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid status value: " + status));
                }
            }
            
            // Get articles based on filters
            Page<NewsArticleDto> articles;
            if (search != null && !search.trim().isEmpty()) {
                // TODO: Implement advanced search with filters in service
                articles = newsService.searchArticles(search.trim(), pageable);
            } else {
                articles = newsService.findAllForAdmin(newsStatus, pageable);
            }
            
            // Create response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("content", articles.getContent());
            response.put("currentPage", articles.getNumber());
            response.put("totalPages", articles.getTotalPages());
            response.put("totalElements", articles.getTotalElements());
            response.put("size", articles.getSize());
            response.put("first", articles.isFirst());
            response.put("last", articles.isLast());
            response.put("empty", articles.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting articles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh sách bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Get article by ID
     * 
     * @param id Article ID
     * @return Article details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {
        try {
            log.info("Getting article with id: {}", id);
            
            Optional<NewsArticleDto> article = newsService.findById(id);
            if (article.isPresent()) {
                return ResponseEntity.ok(article.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Create new article via form submission
     * 
     * @param title Article title
     * @param slug Article slug
     * @param content Article content
     * @param excerpt Article excerpt
     * @param categoryId Category ID
     * @param status Article status
     * @param isFeatured Featured flag
     * @param metaDescription Meta description
     * @param metaKeywords Meta keywords
     * @param publishedAt Published date
     * @param featuredImage Featured image file
     * @return Created article
     */
    @PostMapping("/create")
    public ResponseEntity<?> createArticleForm(
            @RequestParam("title") String title,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam("content") String content,
            @RequestParam(value = "excerpt", required = false) String excerpt,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "status", defaultValue = "DRAFT") String status,
            @RequestParam(value = "isFeatured", defaultValue = "false") Boolean isFeatured,
            @RequestParam(value = "metaDescription", required = false) String metaDescription,
            @RequestParam(value = "metaKeywords", required = false) String metaKeywords,
            @RequestParam(value = "publishedAt", required = false) String publishedAt,
            @RequestParam(value = "featuredImage", required = false) MultipartFile featuredImage) {
        
        try {
            log.info("Creating new article with form data - title: {}", title);
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Tiêu đề không được để trống"));
            }
            if (categoryId == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Danh mục không được để trống"));
            }
            
            // Get current user as author
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long authorId = 1L; // Default to admin user, should be extracted from auth
            
            // Create article DTO
            NewsArticleDto articleDto = new NewsArticleDto();
            articleDto.setTitle(title);
            articleDto.setSlug(slug);
            articleDto.setContent(content);
            articleDto.setExcerpt(excerpt);
            articleDto.setCategoryId(categoryId);
            articleDto.setAuthorId(authorId);
            articleDto.setIsFeatured(isFeatured != null ? isFeatured : false);
            articleDto.setMetaDescription(metaDescription);
            articleDto.setMetaKeywords(metaKeywords);
            
            // Parse status
            try {
                articleDto.setStatus(status != null ? NewsStatus.valueOf(status.toUpperCase()) : NewsStatus.DRAFT);
            } catch (IllegalArgumentException e) {
                articleDto.setStatus(NewsStatus.DRAFT);
            }
            
            // Parse published date
            if (publishedAt != null && !publishedAt.trim().isEmpty()) {
                try {
                    LocalDateTime publishDate = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    articleDto.setPublishedAt(publishDate);
                } catch (Exception e) {
                    log.warn("Invalid publishedAt format: {}", publishedAt);
                }
            }
            
            // Handle featured image upload
            if (featuredImage != null && !featuredImage.isEmpty()) {
                try {
                    String imageUrl = saveImage(featuredImage);
                    articleDto.setFeaturedImage(imageUrl);
                } catch (IOException e) {
                    log.error("Error saving featured image: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Lỗi khi lưu hình ảnh: " + e.getMessage()));
                }
            }
            
            // Create article
            NewsArticleDto createdArticle = newsService.createArticle(articleDto);
            
            log.info("Created article with id: {}", createdArticle.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for article creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating article from form: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tạo bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Update existing article (JSON)
     * 
     * @param id Article ID
     * @param articleDto Updated article data
     * @param bindingResult Validation results
     * @return Updated article
     */
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<?> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody NewsArticleDto articleDto,
            BindingResult bindingResult) {
        
        try {
            log.info("Updating article with JSON data - id: {}", id);
            
            // Check validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(createValidationErrorResponse(bindingResult));
            }
            
            // Update article
            NewsArticleDto updatedArticle = newsService.updateArticle(id, articleDto);
            
            log.info("Updated article with id: {}", id);
            return ResponseEntity.ok(updatedArticle);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for article update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Update existing article (Multipart Form) - separate endpoint
     * 
     * @param id Article ID
     * @param title Article title
     * @param slug Article slug
     * @param content Article content
     * @param excerpt Article excerpt
     * @param categoryId Category ID
     * @param status Article status
     * @param isFeatured Featured flag
     * @param metaDescription Meta description
     * @param metaKeywords Meta keywords
     * @param publishedAt Published date
     * @param featuredImage Featured image file
     * @return Updated article
     */
    @PostMapping(value = "/{id}/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateArticleForm(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam("content") String content,
            @RequestParam(value = "excerpt", required = false) String excerpt,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "status", defaultValue = "DRAFT") String status,
            @RequestParam(value = "isFeatured", defaultValue = "false") Boolean isFeatured,
            @RequestParam(value = "metaDescription", required = false) String metaDescription,
            @RequestParam(value = "metaKeywords", required = false) String metaKeywords,
            @RequestParam(value = "publishedAt", required = false) String publishedAt,
            @RequestParam(value = "featuredImage", required = false) MultipartFile featuredImage) {
        
        try {
            log.info("=== MULTIPART UPDATE REQUEST RECEIVED ===");
            log.info("Article ID: {}", id);
            log.info("Title: {}", title);
            log.info("Category ID: {}", categoryId);
            log.info("Status: {}", status);
            log.info("Featured Image: {}", featuredImage != null ? featuredImage.getOriginalFilename() : "null");
            log.info("Request received at endpoint: /api/admin/news/{}/update", id);
            log.info("============================================");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Tiêu đề không được để trống"));
            }
            if (categoryId == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Danh mục không được để trống"));
            }
            
            // Get existing article to preserve author
            Optional<NewsArticleDto> existingArticleOpt = newsService.findById(id);
            if (existingArticleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Không tìm thấy bài viết với ID: " + id));
            }
            
            NewsArticleDto existingArticle = existingArticleOpt.get();
            
            // Create updated article DTO
            NewsArticleDto articleDto = new NewsArticleDto();
            articleDto.setId(id);
            articleDto.setTitle(title);
            articleDto.setSlug(slug);
            articleDto.setContent(content);
            articleDto.setExcerpt(excerpt);
            articleDto.setCategoryId(categoryId);
            articleDto.setAuthorId(existingArticle.getAuthorId()); // Preserve original author
            articleDto.setIsFeatured(isFeatured != null ? isFeatured : false);
            articleDto.setMetaDescription(metaDescription);
            articleDto.setMetaKeywords(metaKeywords);
            
            // Parse status
            try {
                articleDto.setStatus(status != null ? NewsStatus.valueOf(status.toUpperCase()) : NewsStatus.DRAFT);
            } catch (IllegalArgumentException e) {
                articleDto.setStatus(NewsStatus.DRAFT);
            }
            
            // Parse published date
            if (publishedAt != null && !publishedAt.trim().isEmpty()) {
                try {
                    LocalDateTime publishDate = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    articleDto.setPublishedAt(publishDate);
                } catch (Exception e) {
                    log.warn("Invalid publishedAt format: {}", publishedAt);
                }
            }
            
            // Handle featured image upload
            if (featuredImage != null && !featuredImage.isEmpty()) {
                try {
                    log.info("Processing featured image upload...");
                    String imageUrl = saveImage(featuredImage);
                    articleDto.setFeaturedImage(imageUrl);
                    log.info("Featured image saved successfully: {}", imageUrl);
                } catch (Exception e) {
                    log.error("Error saving featured image: {}", e.getMessage(), e);
                    // Don't fail the entire update if image upload fails
                    // Just preserve the existing image
                    articleDto.setFeaturedImage(existingArticle.getFeaturedImage());
                    log.warn("Using existing featured image due to upload error");
                }
            } else {
                // Preserve existing featured image if no new image uploaded
                articleDto.setFeaturedImage(existingArticle.getFeaturedImage());
                log.info("No new image uploaded, preserving existing image");
            }
            
            // Update article
            NewsArticleDto updatedArticle = newsService.updateArticle(id, articleDto);
            
            log.info("Updated article with id: {}", id);
            return ResponseEntity.ok(updatedArticle);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for article update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating article from form: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Delete article (soft delete - set status to ARCHIVED)
     * 
     * @param id Article ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            log.info("=== DELETE REQUEST RECEIVED ===");
            log.info("Article ID: {}", id);
            log.info("User roles: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            log.info("===============================");
            
            newsService.deleteArticle(id);
            
            log.info("Successfully deleted article with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Bài viết đã được xóa thành công"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Article not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy bài viết với ID: " + id));
        } catch (Exception e) {
            log.error("Error deleting article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi xóa bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Publish article
     * 
     * @param id Article ID
     * @return Success message
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishArticle(@PathVariable Long id) {
        try {
            log.info("Publishing article with id: {}", id);
            
            newsService.publishArticle(id);
            
            log.info("Published article with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Bài viết đã được xuất bản"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Article not found for publishing: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error publishing article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi xuất bản bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Unpublish article
     * 
     * @param id Article ID
     * @return Success message
     */
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublishArticle(@PathVariable Long id) {
        try {
            log.info("Unpublishing article with id: {}", id);
            
            newsService.unpublishArticle(id);
            
            log.info("Unpublished article with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Bài viết đã được hủy xuất bản"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Article not found for unpublishing: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error unpublishing article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi hủy xuất bản bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Toggle featured status of article
     * 
     * @param id Article ID
     * @return Success message
     */
    @PostMapping("/{id}/feature")
    public ResponseEntity<?> toggleFeatured(@PathVariable Long id) {
        try {
            log.info("Toggling featured status for article with id: {}", id);
            
            newsService.toggleFeatured(id);
            
            log.info("Toggled featured status for article with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Trạng thái nổi bật đã được cập nhật"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Article not found for feature toggle: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error toggling featured status for article {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật trạng thái nổi bật: " + e.getMessage()));
        }
    }
    
    /**
     * Get analytics data for news management
     * 
     * @return Analytics data including statistics and charts
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        try {
            log.info("Getting news analytics");
            
            Map<String, Object> analytics = new HashMap<>();
            
            // Article statistics by status
            Map<String, Long> statusStats = newsService.getArticleStatsByStatus();
            analytics.put("statusStats", statusStats);
            
            // View statistics by month (last 6 months)
            Map<String, Long> viewStats = newsService.getViewStatsByMonth(6);
            analytics.put("viewStats", viewStats);
            
            // Most viewed articles (top 10)
            List<NewsArticleDto> mostViewed = newsService.findMostViewedArticles(10);
            analytics.put("mostViewedArticles", mostViewed);
            
            // Recent articles (last 5)
            List<NewsArticleDto> recentArticles = newsService.findRecentArticles(5);
            analytics.put("recentArticles", recentArticles);
            
            // Featured articles
            List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(5);
            analytics.put("featuredArticles", featuredArticles);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error getting analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải thống kê: " + e.getMessage()));
        }
    }
    
    /**
     * Bulk operations on articles
     * 
     * @param operation Operation type (publish, unpublish, delete, feature, unfeature)
     * @param articleIds List of article IDs
     * @return Success message with operation results
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkOperation(
            @RequestParam String operation,
            @RequestBody List<Long> articleIds) {
        
        try {
            log.info("Performing bulk operation '{}' on {} articles", operation, articleIds.size());
            
            if (articleIds == null || articleIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Danh sách bài viết không được để trống"));
            }
            
            int successCount = 0;
            int errorCount = 0;
            
            for (Long articleId : articleIds) {
                try {
                    switch (operation.toLowerCase()) {
                        case "publish":
                            newsService.publishArticle(articleId);
                            break;
                        case "unpublish":
                            newsService.unpublishArticle(articleId);
                            break;
                        case "delete":
                            newsService.deleteArticle(articleId);
                            break;
                        case "feature":
                            newsService.toggleFeatured(articleId);
                            break;
                        default:
                            return ResponseEntity.badRequest()
                                .body(createErrorResponse("Thao tác không hợp lệ: " + operation));
                    }
                    successCount++;
                } catch (Exception e) {
                    log.warn("Error in bulk operation for article {}: {}", articleId, e.getMessage());
                    errorCount++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", String.format("Hoàn thành: %d thành công, %d lỗi", successCount, errorCount));
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk operation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi thực hiện thao tác hàng loạt: " + e.getMessage()));
        }
    }
    
    /**
     * Save uploaded image to disk
     */
    private String saveImage(MultipartFile file) throws IOException {
        try {
            log.info("Starting image save process...");
            log.info("File info - Name: {}, Size: {} bytes, ContentType: {}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            
            // Validate file
            if (file.isEmpty()) {
                throw new IOException("File is empty");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IOException("File size exceeds 5MB limit");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IOException("File is not an image");
            }
            
            // Create upload directory if not exists
            Path uploadDir = Paths.get(uploadPath);
            log.info("Upload directory: {}", uploadDir.toAbsolutePath());
            
            if (!Files.exists(uploadDir)) {
                log.info("Creating upload directory...");
                Files.createDirectories(uploadDir);
            }
            
            // Check if directory is writable
            if (!Files.isWritable(uploadDir)) {
                throw new IOException("Upload directory is not writable: " + uploadDir.toAbsolutePath());
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg"; // Default
            
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                // Validate extension
                if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                    extension = ".jpg";
                }
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadDir.resolve(filename);
            log.info("Saving file to: {}", filePath.toAbsolutePath());
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Verify file was saved
            if (!Files.exists(filePath)) {
                throw new IOException("File was not saved successfully");
            }
            
            // Return relative URL
            String imageUrl = "/uploads/news/" + filename;
            log.info("Image saved successfully: {}", imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("Error in saveImage method: {}", e.getMessage(), e);
            throw new IOException("Không thể lưu hình ảnh: " + e.getMessage(), e);
        }
    }
    
    // Helper methods for response creation
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Dữ liệu không hợp lệ");
        
        Map<String, String> fieldErrors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage()));
        
        response.put("fieldErrors", fieldErrors);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}