package com.dacsanviet.validation;

import com.dacsanviet.dto.NewsArticleDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test custom validation annotations for news management
 * Validates: Requirements 1.5 (Input validation)
 */
@SpringBootTest
@ActiveProfiles("test")
public class ValidationTest {
    
    @Autowired
    private Validator validator;
    
    /**
     * Test valid NewsArticleDto passes validation
     */
    @Test
    public void testValidNewsArticleDto() {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setTitle("Valid News Title");
        dto.setSlug("valid-news-title");
        dto.setContent("<p>This is valid HTML content</p>");
        dto.setExcerpt("Valid excerpt");
        dto.setMetaDescription("Valid meta description under 160 characters");
        
        Set<ConstraintViolation<NewsArticleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should not have violations");
    }
    
    /**
     * Test invalid slug fails validation
     */
    @Test
    public void testInvalidSlugValidation() {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setTitle("Valid News Title");
        dto.setSlug("Invalid Slug With Spaces");
        dto.setContent("<p>Valid content</p>");
        
        Set<ConstraintViolation<NewsArticleDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("slug")),
                "Invalid slug should cause validation error");
    }
    
    /**
     * Test dangerous HTML content fails validation
     */
    @Test
    public void testDangerousHtmlValidation() {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setTitle("Valid News Title");
        dto.setSlug("valid-slug");
        dto.setContent("<script>alert('xss')</script><p>Content</p>");
        
        Set<ConstraintViolation<NewsArticleDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")),
                "Dangerous HTML should cause validation error");
    }
    
    /**
     * Test title length validation
     */
    @Test
    public void testTitleLengthValidation() {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setTitle(""); // Too short
        dto.setSlug("valid-slug");
        dto.setContent("<p>Valid content</p>");
        
        Set<ConstraintViolation<NewsArticleDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")),
                "Empty title should cause validation error");
    }
    
    /**
     * Test meta description length validation
     */
    @Test
    public void testMetaDescriptionLengthValidation() {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setTitle("Valid News Title");
        dto.setSlug("valid-slug");
        dto.setContent("<p>Valid content</p>");
        // Create a meta description that's definitely over 160 characters
        String longDescription = "This is a very long meta description that exceeds the maximum allowed length of 160 characters. " +
                                "It should definitely cause a validation error when validated by the system because it's way too long for SEO purposes.";
        dto.setMetaDescription(longDescription);
        
        Set<ConstraintViolation<NewsArticleDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("metaDescription")),
                "Long meta description (" + longDescription.length() + " chars) should cause validation error");
    }
}