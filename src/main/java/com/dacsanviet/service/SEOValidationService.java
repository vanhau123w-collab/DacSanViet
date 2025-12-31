package com.dacsanviet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for comprehensive SEO validation
 * Provides validation methods for SEO-related fields and content
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SEOValidationService {
    
    private final SEOService seoService;
    
    /**
     * Validate all SEO-related fields for a news article
     * @param title Article title
     * @param metaDescription Meta description
     * @param metaKeywords Meta keywords
     * @param slug Article slug (optional, will be generated if null)
     * @return Comprehensive validation result
     */
    public SEOValidationResult validateArticleSEO(String title, String metaDescription, 
                                                  String metaKeywords, String slug) {
        SEOValidationResult result = new SEOValidationResult();
        
        // Validate title
        if (!StringUtils.hasText(title)) {
            result.addError("Title is required");
        } else if (title.length() < 5) {
            result.addError("Title must be at least 5 characters long");
        } else if (title.length() > 200) {
            result.addError("Title must not exceed 200 characters");
        } else if (title.length() > 60) {
            result.addWarning("Title is longer than 60 characters and may be truncated in search results");
        }
        
        // Validate meta description
        if (StringUtils.hasText(metaDescription)) {
            SEOService.MetaDescriptionValidationResult metaResult = 
                seoService.validateMetaDescription(metaDescription);
            
            if (!metaResult.isValid()) {
                result.addErrors(metaResult.getMessages());
            }
            
            if (metaResult.hasWarnings()) {
                result.addWarnings(metaResult.getWarnings());
            }
        } else {
            result.addWarning("Meta description is empty - consider adding one for better SEO");
        }
        
        // Validate meta keywords
        if (StringUtils.hasText(metaKeywords)) {
            if (metaKeywords.length() > 255) {
                result.addError("Meta keywords must not exceed 255 characters");
            }
            
            String[] keywords = metaKeywords.split(",");
            if (keywords.length > 10) {
                result.addWarning("Consider using fewer than 10 keywords for better SEO");
            }
            
            for (String keyword : keywords) {
                String trimmed = keyword.trim();
                if (trimmed.length() > 50) {
                    result.addWarning("Keyword '" + trimmed + "' is very long - consider shorter keywords");
                }
            }
        }
        
        // Validate slug if provided
        if (StringUtils.hasText(slug)) {
            if (!seoService.isValidSlug(slug)) {
                result.addError("Slug contains invalid characters. Use only lowercase letters, numbers, and hyphens");
            }
        }
        
        // Generate SEO recommendations
        generateSEORecommendations(title, metaDescription, metaKeywords, result);
        
        return result;
    }
    
    /**
     * Generate SEO recommendations based on content analysis
     */
    private void generateSEORecommendations(String title, String metaDescription, 
                                          String metaKeywords, SEOValidationResult result) {
        
        // Title recommendations
        if (StringUtils.hasText(title)) {
            if (!title.matches(".*[0-9].*") && !title.toLowerCase().contains("năm")) {
                result.addRecommendation("Consider adding current year or numbers to title for better engagement");
            }
            
            if (!title.toLowerCase().matches(".*(cách|hướng dẫn|top|tốt nhất|mới nhất).*")) {
                result.addRecommendation("Consider using engaging words like 'cách', 'hướng dẫn', 'top', 'tốt nhất' in title");
            }
        }
        
        // Meta description recommendations
        if (StringUtils.hasText(metaDescription)) {
            if (!metaDescription.toLowerCase().contains("đặc sản việt")) {
                result.addRecommendation("Consider mentioning 'Đặc Sản Việt' in meta description for brand recognition");
            }
            
            if (!metaDescription.matches(".*[!?].*")) {
                result.addRecommendation("Consider adding a call-to-action or question mark in meta description");
            }
        }
        
        // Keywords recommendations
        if (!StringUtils.hasText(metaKeywords)) {
            result.addRecommendation("Add relevant keywords to improve search visibility");
        }
    }
    
    /**
     * Validate slug uniqueness
     * @param slug The slug to validate
     * @param excludeId Article ID to exclude from uniqueness check
     * @return true if slug is unique, false otherwise
     */
    public boolean isSlugUnique(String slug, Long excludeId) {
        return !seoService.isSlugExists(slug, excludeId);
    }
    
    /**
     * Generate SEO preview for article
     * @param title Article title
     * @param metaDescription Meta description
     * @param slug Article slug
     * @return Formatted SEO preview
     */
    public String generateSEOPreview(String title, String metaDescription, String slug) {
        return seoService.generateSearchPreview(title, metaDescription, slug);
    }
    
    /**
     * Result class for comprehensive SEO validation
     */
    public static class SEOValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void addError(String error) {
            this.errors.add(error);
        }
        
        public void addErrors(List<String> errors) {
            this.errors.addAll(errors);
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public void addWarnings(List<String> warnings) {
            this.warnings.addAll(warnings);
        }
        
        public List<String> getRecommendations() {
            return recommendations;
        }
        
        public void addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean hasRecommendations() {
            return !recommendations.isEmpty();
        }
        
        public String getAllMessages() {
            StringBuilder sb = new StringBuilder();
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("- ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("- ").append(warning).append("\n");
                }
            }
            
            if (!recommendations.isEmpty()) {
                sb.append("Recommendations:\n");
                for (String recommendation : recommendations) {
                    sb.append("- ").append(recommendation).append("\n");
                }
            }
            
            return sb.toString().trim();
        }
    }
}