package com.dacsanviet.service;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.NewsArticleRepository;
import com.dacsanviet.repository.NewsCategoryRepository;
import com.dacsanviet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration tests for NewsService SEO functionality
 */
@ExtendWith(MockitoExtension.class)
class NewsServiceSEOIntegrationTest {
    
    @Mock
    private NewsArticleRepository newsArticleRepository;
    
    @Mock
    private NewsCategoryRepository newsCategoryRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SEOService seoService;
    
    private NewsService newsService;
    
    @BeforeEach
    void setUp() {
        newsService = new NewsService(newsArticleRepository, newsCategoryRepository, userRepository, seoService);
    }
    
    @Test
    void testCreateArticle_WithValidMetaDescription() {
        // Setup test data
        User author = new User();
        author.setId(1L);
        author.setUsername("admin");
        
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Bánh chưng truyền thống Việt Nam");
        articleDto.setContent("Nội dung bài viết về bánh chưng...");
        articleDto.setMetaDescription("Khám phá cách làm bánh chưng truyền thống với hương vị đậm đà, mang đến không khí tết cổ truyền cho gia đình bạn.");
        articleDto.setAuthorId(1L);
        articleDto.setStatus(NewsStatus.DRAFT);
        
        // Mock dependencies
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(seoService.generateUniqueSlug(anyString())).thenReturn("banh-chung-truyen-thong-viet-nam");
        
        SEOService.MetaDescriptionValidationResult validationResult = new SEOService.MetaDescriptionValidationResult();
        validationResult.setValid(true);
        when(seoService.validateMetaDescription(anyString())).thenReturn(validationResult);
        
        when(newsArticleRepository.save(any())).thenAnswer(invocation -> {
            com.dacsanviet.model.NewsArticle article = invocation.getArgument(0);
            article.setId(1L);
            return article;
        });
        
        // Execute
        NewsArticleDto result = newsService.createArticle(articleDto);
        
        // Verify
        assertNotNull(result);
        assertEquals("Bánh chưng truyền thống Việt Nam", result.getTitle());
        assertEquals("banh-chung-truyen-thong-viet-nam", result.getSlug());
    }
    
    @Test
    void testCreateArticle_WithInvalidMetaDescription() {
        // Setup test data
        User author = new User();
        author.setId(1L);
        author.setUsername("admin");
        
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Bánh chưng truyền thống Việt Nam");
        articleDto.setContent("Nội dung bài viết về bánh chưng...");
        articleDto.setMetaDescription("Quá ngắn"); // Too short meta description
        articleDto.setAuthorId(1L);
        articleDto.setStatus(NewsStatus.DRAFT);
        
        // Mock dependencies
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(seoService.generateUniqueSlug(anyString())).thenReturn("banh-chung-truyen-thong-viet-nam");
        
        SEOService.MetaDescriptionValidationResult validationResult = new SEOService.MetaDescriptionValidationResult();
        validationResult.setValid(false);
        validationResult.addMessage("Meta description is too short");
        when(seoService.validateMetaDescription(anyString())).thenReturn(validationResult);
        
        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            newsService.createArticle(articleDto);
        });
        
        assertTrue(exception.getMessage().contains("Invalid meta description"));
    }
    
    @Test
    void testUpdateArticle_TitleChanged_SlugUpdated() {
        // Setup test data
        com.dacsanviet.model.NewsArticle existingArticle = new com.dacsanviet.model.NewsArticle();
        existingArticle.setId(1L);
        existingArticle.setTitle("Old Title");
        existingArticle.setSlug("old-title");
        
        User author = new User();
        author.setId(1L);
        author.setUsername("admin");
        existingArticle.setAuthor(author);
        
        NewsArticleDto updateDto = new NewsArticleDto();
        updateDto.setTitle("New Title - Updated");
        updateDto.setContent("Updated content");
        updateDto.setStatus(NewsStatus.DRAFT);
        
        // Mock dependencies
        when(newsArticleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));
        when(seoService.generateUniqueSlug("New Title - Updated", 1L)).thenReturn("new-title-updated");
        when(newsArticleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        NewsArticleDto result = newsService.updateArticle(1L, updateDto);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Title - Updated", result.getTitle());
        assertEquals("new-title-updated", result.getSlug());
    }
}