package com.dacsanviet.service;

import com.dacsanviet.repository.NewsArticleRepository;
import com.dacsanviet.model.NewsArticle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SEOService
 */
@ExtendWith(MockitoExtension.class)
class SEOServiceTest {
    
    @Mock
    private NewsArticleRepository newsArticleRepository;
    
    private SEOService seoService;
    
    @BeforeEach
    void setUp() {
        seoService = new SEOService(newsArticleRepository);
    }
    
    @Test
    void testCreateSlugFromTitle_BasicVietnameseTitle() {
        // Test basic Vietnamese title conversion
        String title = "Đặc sản Việt Nam ngon nhất";
        String slug = seoService.createSlugFromTitle(title);
        
        assertEquals("dac-san-viet-nam-ngon-nhat", slug);
    }
    
    @Test
    void testCreateSlugFromTitle_ComplexVietnameseTitle() {
        // Test complex Vietnamese title with various accents
        String title = "Bánh chưng truyền thống - Hương vị tết cổ truyền";
        String slug = seoService.createSlugFromTitle(title);
        
        assertEquals("banh-chung-truyen-thong-huong-vi-tet-co-truyen", slug);
    }
    
    @Test
    void testCreateSlugFromTitle_WithSpecialCharacters() {
        // Test title with special characters
        String title = "Top 10 món ăn ngon (2024) - 100% tự nhiên!";
        String slug = seoService.createSlugFromTitle(title);
        
        assertEquals("top-10-mon-an-ngon-2024-100-tu-nhien", slug);
    }
    
    @Test
    void testCreateSlugFromTitle_EmptyTitle() {
        // Test empty title
        assertThrows(IllegalArgumentException.class, () -> {
            seoService.createSlugFromTitle("");
        });
    }
    
    @Test
    void testCreateSlugFromTitle_NullTitle() {
        // Test null title
        assertThrows(IllegalArgumentException.class, () -> {
            seoService.createSlugFromTitle(null);
        });
    }
    
    @Test
    void testGenerateUniqueSlug_NoConflict() {
        // Mock no existing slug
        when(newsArticleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        
        String title = "Bánh mì Việt Nam";
        String slug = seoService.generateUniqueSlug(title);
        
        assertEquals("banh-mi-viet-nam", slug);
    }
    
    @Test
    void testGenerateUniqueSlug_WithConflict() {
        // Mock existing slug
        NewsArticle existingArticle = new NewsArticle();
        existingArticle.setId(1L);
        existingArticle.setSlug("banh-mi-viet-nam");
        
        when(newsArticleRepository.findBySlug("banh-mi-viet-nam"))
            .thenReturn(Optional.of(existingArticle));
        when(newsArticleRepository.findBySlug("banh-mi-viet-nam-1"))
            .thenReturn(Optional.empty());
        
        String title = "Bánh mì Việt Nam";
        String slug = seoService.generateUniqueSlug(title);
        
        assertEquals("banh-mi-viet-nam-1", slug);
    }
    
    @Test
    void testIsValidSlug_ValidSlugs() {
        assertTrue(seoService.isValidSlug("banh-mi-viet-nam"));
        assertTrue(seoService.isValidSlug("top-10-mon-an-2024"));
        assertTrue(seoService.isValidSlug("dac-san-mien-bac"));
    }
    
    @Test
    void testIsValidSlug_InvalidSlugs() {
        assertFalse(seoService.isValidSlug("Bánh-mì")); // uppercase
        assertFalse(seoService.isValidSlug("banh_mi")); // underscore
        assertFalse(seoService.isValidSlug("banh mi")); // space
        assertFalse(seoService.isValidSlug("")); // empty
        assertFalse(seoService.isValidSlug(null)); // null
    }
    
    @Test
    void testValidateMetaDescription_ValidLength() {
        String metaDescription = "Khám phá những món đặc sản Việt Nam ngon nhất với hương vị truyền thống được chế biến từ nguyên liệu tự nhiên, mang đến trải nghiệm ẩm thực tuyệt vời.";
        
        SEOService.MetaDescriptionValidationResult result = seoService.validateMetaDescription(metaDescription);
        
        assertTrue(result.isValid());
        assertFalse(result.getMessages().isEmpty());
    }
    
    @Test
    void testValidateMetaDescription_TooShort() {
        String metaDescription = "Món ăn ngon";
        
        SEOService.MetaDescriptionValidationResult result = seoService.validateMetaDescription(metaDescription);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessages().get(0).contains("too short"));
    }
    
    @Test
    void testValidateMetaDescription_TooLong() {
        String metaDescription = "Đây là một mô tả rất dài về các món đặc sản Việt Nam ngon nhất với hương vị truyền thống được chế biến từ nguyên liệu tự nhiên hoàn toàn không có chất bảo quản, mang đến cho bạn những trải nghiệm ẩm thực tuyệt vời nhất mà bạn chưa từng thử qua.";
        
        SEOService.MetaDescriptionValidationResult result = seoService.validateMetaDescription(metaDescription);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessages().get(0).contains("too long"));
    }
    
    @Test
    void testValidateMetaDescription_Empty() {
        SEOService.MetaDescriptionValidationResult result = seoService.validateMetaDescription("");
        
        assertTrue(result.isValid()); // Empty is allowed
        assertTrue(result.getMessages().get(0).contains("empty"));
    }
    
    @Test
    void testGenerateSearchPreview() {
        String title = "Bánh chưng truyền thống Việt Nam";
        String metaDescription = "Khám phá cách làm bánh chưng truyền thống với hương vị đậm đà, mang đến không khí tết cổ truyền cho gia đình bạn.";
        String slug = "banh-chung-truyen-thong-viet-nam";
        
        String preview = seoService.generateSearchPreview(title, metaDescription, slug);
        
        assertNotNull(preview);
        assertTrue(preview.contains(title));
        assertTrue(preview.contains(slug));
        assertTrue(preview.contains(metaDescription));
        assertTrue(preview.contains("dacsanviet.com"));
    }
}