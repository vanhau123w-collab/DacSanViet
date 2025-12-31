package com.dacsanviet.service;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.model.NewsArticle;
import com.dacsanviet.model.NewsCategory;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.NewsArticleRepository;
import com.dacsanviet.repository.NewsCategoryRepository;
import com.dacsanviet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing news articles
 * Handles CRUD operations, search, filtering, and business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsService {
    
    private final NewsArticleRepository newsArticleRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final UserRepository userRepository;
    private final SEOService seoService;
    
    // CRUD Operations
    
    /**
     * Create a new news article
     */
    public NewsArticleDto createArticle(NewsArticleDto articleDto) {
        log.info("Creating new article with title: {}", articleDto.getTitle());
        
        // Validate author exists
        User author = userRepository.findById(articleDto.getAuthorId())
            .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + articleDto.getAuthorId()));
        
        // Validate category if provided
        NewsCategory category = null;
        if (articleDto.getCategoryId() != null) {
            category = newsCategoryRepository.findById(articleDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + articleDto.getCategoryId()));
        }
        
        // Generate unique slug using SEOService
        String slug = seoService.generateUniqueSlug(articleDto.getTitle());
        
        // Validate meta description if provided
        if (StringUtils.hasText(articleDto.getMetaDescription())) {
            SEOService.MetaDescriptionValidationResult validationResult = 
                seoService.validateMetaDescription(articleDto.getMetaDescription());
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("Invalid meta description: " + validationResult.getAllMessages());
            }
        }
        
        // Create article entity
        NewsArticle article = new NewsArticle();
        article.setTitle(articleDto.getTitle());
        article.setSlug(slug);
        article.setContent(articleDto.getContent());
        article.setExcerpt(articleDto.getExcerpt());
        article.setFeaturedImage(articleDto.getFeaturedImage());
        article.setThumbnailImage(articleDto.getThumbnailImage());
        article.setStatus(articleDto.getStatus() != null ? articleDto.getStatus() : NewsStatus.DRAFT);
        article.setCategory(category);
        article.setAuthor(author);
        article.setIsFeatured(articleDto.getIsFeatured() != null ? articleDto.getIsFeatured() : false);
        article.setMetaDescription(articleDto.getMetaDescription());
        article.setMetaKeywords(articleDto.getMetaKeywords());
        
        // Set published date if status is PUBLISHED
        if (article.getStatus() == NewsStatus.PUBLISHED && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        NewsArticle savedArticle = newsArticleRepository.save(article);
        log.info("Created article with id: {} and slug: {}", savedArticle.getId(), savedArticle.getSlug());
        
        return convertToDto(savedArticle);
    }
    
    /**
     * Update an existing news article
     */
    public NewsArticleDto updateArticle(Long id, NewsArticleDto articleDto) {
        log.info("Updating article with id: {}", id);
        
        NewsArticle article = newsArticleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        // Validate category if provided
        NewsCategory category = null;
        if (articleDto.getCategoryId() != null) {
            category = newsCategoryRepository.findById(articleDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + articleDto.getCategoryId()));
        }
        
        // Update slug if title changed using SEOService
        if (!article.getTitle().equals(articleDto.getTitle())) {
            String newSlug = seoService.generateUniqueSlug(articleDto.getTitle(), id);
            article.setSlug(newSlug);
        }
        
        // Validate meta description if provided
        if (StringUtils.hasText(articleDto.getMetaDescription())) {
            SEOService.MetaDescriptionValidationResult validationResult = 
                seoService.validateMetaDescription(articleDto.getMetaDescription());
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("Invalid meta description: " + validationResult.getAllMessages());
            }
        }
        
        // Update fields
        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());
        article.setExcerpt(articleDto.getExcerpt());
        article.setFeaturedImage(articleDto.getFeaturedImage());
        article.setThumbnailImage(articleDto.getThumbnailImage());
        article.setCategory(category);
        article.setIsFeatured(articleDto.getIsFeatured() != null ? articleDto.getIsFeatured() : false);
        article.setMetaDescription(articleDto.getMetaDescription());
        article.setMetaKeywords(articleDto.getMetaKeywords());
        
        // Handle status change
        NewsStatus oldStatus = article.getStatus();
        NewsStatus newStatus = articleDto.getStatus() != null ? articleDto.getStatus() : oldStatus;
        
        if (oldStatus != newStatus) {
            article.setStatus(newStatus);
            // Set published date when publishing for the first time
            if (newStatus == NewsStatus.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        
        NewsArticle savedArticle = newsArticleRepository.save(article);
        log.info("Updated article with id: {}", savedArticle.getId());
        
        return convertToDto(savedArticle);
    }
    
    /**
     * Soft delete an article (set status to ARCHIVED)
     */
    public void deleteArticle(Long id) {
        log.info("Soft deleting article with id: {}", id);
        
        NewsArticle article = newsArticleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        article.setStatus(NewsStatus.ARCHIVED);
        newsArticleRepository.save(article);
        
        log.info("Soft deleted article with id: {}", id);
    }
    
    /**
     * Find article by ID
     */
    @Transactional(readOnly = true)
    public Optional<NewsArticleDto> findById(Long id) {
        return newsArticleRepository.findById(id)
            .map(this::convertToDto);
    }
    
    /**
     * Find article by slug
     */
    @Transactional(readOnly = true)
    public Optional<NewsArticleDto> findBySlug(String slug) {
        return newsArticleRepository.findBySlug(slug)
            .map(this::convertToDto);
    }
    
    // Query Operations
    
    /**
     * Find published articles with pagination
     */
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> findPublishedArticles(Pageable pageable) {
        return newsArticleRepository.findPublishedArticles(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find articles by category
     */
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> findByCategory(Long categoryId, Pageable pageable) {
        return newsArticleRepository.findPublishedArticlesByCategory(categoryId, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find articles by category slug
     */
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> findByCategorySlug(String categorySlug, Pageable pageable) {
        return newsArticleRepository.findPublishedArticlesByCategorySlug(categorySlug, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Search articles by keyword
     */
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> searchArticles(String keyword, Pageable pageable) {
        return newsArticleRepository.searchPublishedArticles(keyword, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find featured articles
     */
    @Transactional(readOnly = true)
    public List<NewsArticleDto> findFeaturedArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsArticleRepository.findFeaturedArticles(pageable)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Find recent articles
     */
    @Transactional(readOnly = true)
    public List<NewsArticleDto> findRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsArticleRepository.findRecentPublishedArticles(pageable)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    // Admin Operations
    
    /**
     * Find all articles for admin with filtering
     */
    @Transactional(readOnly = true)
    public Page<NewsArticleDto> findAllForAdmin(NewsStatus status, Pageable pageable) {
        if (status != null) {
            return newsArticleRepository.findByStatus(status, pageable)
                .map(this::convertToDto);
        } else {
            return newsArticleRepository.findAll(pageable)
                .map(this::convertToDto);
        }
    }
    
    /**
     * Publish an article
     */
    public void publishArticle(Long id) {
        log.info("Publishing article with id: {}", id);
        
        NewsArticle article = newsArticleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        article.setStatus(NewsStatus.PUBLISHED);
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        newsArticleRepository.save(article);
        log.info("Published article with id: {}", id);
    }
    
    /**
     * Unpublish an article
     */
    public void unpublishArticle(Long id) {
        log.info("Unpublishing article with id: {}", id);
        
        NewsArticle article = newsArticleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        article.setStatus(NewsStatus.DRAFT);
        newsArticleRepository.save(article);
        
        log.info("Unpublished article with id: {}", id);
    }
    
    /**
     * Toggle featured status of an article
     */
    public void toggleFeatured(Long id) {
        log.info("Toggling featured status for article with id: {}", id);
        
        NewsArticle article = newsArticleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        article.setIsFeatured(!article.getIsFeatured());
        newsArticleRepository.save(article);
        
        log.info("Toggled featured status for article with id: {} to {}", id, article.getIsFeatured());
    }
    
    // Analytics
    
    /**
     * Increment view count for an article
     */
    public void incrementViewCount(Long id) {
        log.debug("Incrementing view count for article with id: {}", id);
        newsArticleRepository.incrementViewCount(id);
    }
    
    /**
     * Find most viewed articles
     */
    @Transactional(readOnly = true)
    public List<NewsArticleDto> findMostViewedArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsArticleRepository.findMostViewedArticles(pageable)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get article statistics by status
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getArticleStatsByStatus() {
        List<Object[]> stats = newsArticleRepository.getArticleStatsByStatus();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            NewsStatus status = (NewsStatus) stat[0];
            Long count = (Long) stat[1];
            result.put(status.name(), count);
        }
        
        return result;
    }
    
    /**
     * Get view statistics by month
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getViewStatsByMonth(int months) {
        LocalDateTime sinceDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> stats = newsArticleRepository.getViewStatsByMonth(sinceDate);
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            Integer year = (Integer) stat[0];
            Integer month = (Integer) stat[1];
            Long views = (Long) stat[2];
            String key = String.format("%d-%02d", year, month);
            result.put(key, views);
        }
        
        return result;
    }
    
    // Helper Methods
    
    /**
     * Convert NewsArticle entity to DTO
     */
    private NewsArticleDto convertToDto(NewsArticle article) {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSlug(article.getSlug());
        dto.setContent(article.getContent());
        dto.setExcerpt(article.getExcerpt());
        dto.setFeaturedImage(article.getFeaturedImage());
        dto.setThumbnailImage(article.getThumbnailImage());
        dto.setStatus(article.getStatus());
        dto.setViewCount(article.getViewCount());
        dto.setIsFeatured(article.getIsFeatured());
        dto.setMetaDescription(article.getMetaDescription());
        dto.setMetaKeywords(article.getMetaKeywords());
        dto.setPublishedAt(article.getPublishedAt());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        
        // Set category info
        if (article.getCategory() != null) {
            dto.setCategoryId(article.getCategory().getId());
            dto.setCategoryName(article.getCategory().getName());
            dto.setCategorySlug(article.getCategory().getSlug());
        }
        
        // Set author info
        if (article.getAuthor() != null) {
            dto.setAuthorId(article.getAuthor().getId());
            dto.setAuthorName(article.getAuthor().getFullName() != null ? 
                article.getAuthor().getFullName() : article.getAuthor().getUsername());
        }
        
        return dto;
    }
}