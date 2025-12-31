package com.dacsanviet.service;

import com.dacsanviet.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Service for SEO-related operations including slug generation and metadata validation
 * Handles URL slug creation, uniqueness validation, and SEO metadata processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SEOService {
    
    private final NewsArticleRepository newsArticleRepository;
    
    // Constants for validation
    private static final int MIN_META_DESCRIPTION_LENGTH = 150;
    private static final int MAX_META_DESCRIPTION_LENGTH = 160;
    private static final int MAX_SLUG_LENGTH = 250;
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    
    /**
     * Generate a unique slug from title
     * @param title The article title
     * @return A unique URL-friendly slug
     */
    public String generateUniqueSlug(String title) {
        return generateUniqueSlug(title, null);
    }
    
    /**
     * Generate a unique slug from title, excluding a specific article ID
     * @param title The article title
     * @param excludeId Article ID to exclude from uniqueness check (for updates)
     * @return A unique URL-friendly slug
     */
    public String generateUniqueSlug(String title, Long excludeId) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        
        log.debug("Generating unique slug for title: {}", title);
        
        String baseSlug = createSlugFromTitle(title);
        String slug = baseSlug;
        int counter = 1;
        
        // Keep trying until we find a unique slug
        while (isSlugExists(slug, excludeId)) {
            slug = baseSlug + "-" + counter;
            counter++;
            
            // Prevent infinite loop with a reasonable limit
            if (counter > 1000) {
                throw new RuntimeException("Unable to generate unique slug after 1000 attempts for title: " + title);
            }
        }
        
        log.debug("Generated unique slug: {} for title: {}", slug, title);
        return slug;
    }
    
    /**
     * Create a URL-friendly slug from title
     * Handles Vietnamese characters and special characters
     * @param title The article title
     * @return A URL-friendly slug (not guaranteed to be unique)
     */
    public String createSlugFromTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        
        // Normalize and convert to lowercase
        String slug = title.toLowerCase().trim();
        
        // Remove Vietnamese accents and convert to ASCII equivalents
        slug = removeVietnameseAccents(slug);
        
        // Remove special characters except spaces and hyphens
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        
        // Replace multiple spaces with single space
        slug = slug.replaceAll("\\s+", " ");
        
        // Replace spaces with hyphens
        slug = slug.replace(" ", "-");
        
        // Remove multiple consecutive hyphens
        slug = slug.replaceAll("-+", "-");
        
        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        
        // Ensure slug is not empty
        if (!StringUtils.hasText(slug)) {
            slug = "article";
        }
        
        // Truncate if too long
        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH);
            // Remove trailing hyphen if truncation created one
            slug = slug.replaceAll("-+$", "");
        }
        
        return slug;
    }
    
    /**
     * Validate if a slug is properly formatted
     * @param slug The slug to validate
     * @return true if slug is valid, false otherwise
     */
    public boolean isValidSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            return false;
        }
        
        if (slug.length() > MAX_SLUG_LENGTH) {
            return false;
        }
        
        return SLUG_PATTERN.matcher(slug).matches();
    }
    
    /**
     * Check if a slug already exists in the database
     * @param slug The slug to check
     * @param excludeId Article ID to exclude from the check (for updates)
     * @return true if slug exists, false otherwise
     */
    public boolean isSlugExists(String slug, Long excludeId) {
        if (!StringUtils.hasText(slug)) {
            return false;
        }
        
        return newsArticleRepository.findBySlug(slug)
            .map(article -> excludeId == null || !article.getId().equals(excludeId))
            .orElse(false);
    }
    
    /**
     * Validate meta description according to SEO best practices
     * @param metaDescription The meta description to validate
     * @return ValidationResult containing validation status and messages
     */
    public MetaDescriptionValidationResult validateMetaDescription(String metaDescription) {
        MetaDescriptionValidationResult result = new MetaDescriptionValidationResult();
        
        if (!StringUtils.hasText(metaDescription)) {
            result.setValid(true); // Meta description is optional
            result.addMessage("Meta description is empty - consider adding one for better SEO");
            return result;
        }
        
        int length = metaDescription.trim().length();
        
        if (length < MIN_META_DESCRIPTION_LENGTH) {
            result.setValid(false);
            result.addMessage(String.format("Meta description is too short (%d characters). Recommended: %d-%d characters", 
                length, MIN_META_DESCRIPTION_LENGTH, MAX_META_DESCRIPTION_LENGTH));
        } else if (length > MAX_META_DESCRIPTION_LENGTH) {
            result.setValid(false);
            result.addMessage(String.format("Meta description is too long (%d characters). Recommended: %d-%d characters", 
                length, MIN_META_DESCRIPTION_LENGTH, MAX_META_DESCRIPTION_LENGTH));
        } else {
            result.setValid(true);
            result.addMessage(String.format("Meta description length is optimal (%d characters)", length));
        }
        
        // Check for duplicate sentences or repetitive content
        if (hasRepetitiveContent(metaDescription)) {
            result.addWarning("Meta description may contain repetitive content");
        }
        
        return result;
    }
    
    /**
     * Generate a preview snippet similar to Google search results
     * @param title The article title
     * @param metaDescription The meta description
     * @param slug The article slug
     * @return A formatted preview snippet
     */
    public String generateSearchPreview(String title, String metaDescription, String slug) {
        StringBuilder preview = new StringBuilder();
        
        // Title (truncated if too long)
        String displayTitle = title;
        if (title.length() > 60) {
            displayTitle = title.substring(0, 57) + "...";
        }
        preview.append("Title: ").append(displayTitle).append("\n");
        
        // URL
        preview.append("URL: https://dacsanviet.com/news/").append(slug).append("\n");
        
        // Meta description
        String displayDescription = metaDescription;
        if (StringUtils.hasText(metaDescription)) {
            if (metaDescription.length() > MAX_META_DESCRIPTION_LENGTH) {
                displayDescription = metaDescription.substring(0, MAX_META_DESCRIPTION_LENGTH - 3) + "...";
            }
        } else {
            displayDescription = "No meta description provided";
        }
        preview.append("Description: ").append(displayDescription);
        
        return preview.toString();
    }
    
    /**
     * Remove Vietnamese accents and convert to ASCII equivalents
     * @param text The text to process
     * @return Text with Vietnamese characters converted to ASCII
     */
    private String removeVietnameseAccents(String text) {
        // First, handle specific Vietnamese characters that don't normalize well
        text = text.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        text = text.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        text = text.replaceAll("[ìíịỉĩ]", "i");
        text = text.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        text = text.replaceAll("[ùúụủũưừứựửữ]", "u");
        text = text.replaceAll("[ỳýỵỷỹ]", "y");
        text = text.replaceAll("[đ]", "d");
        
        // Handle uppercase versions
        text = text.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        text = text.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        text = text.replaceAll("[ÌÍỊỈĨ]", "I");
        text = text.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        text = text.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        text = text.replaceAll("[ỲÝỴỶỸ]", "Y");
        text = text.replaceAll("[Đ]", "D");
        
        // Use Java's built-in normalization for any remaining accented characters
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        
        return text;
    }
    
    /**
     * Check if text contains repetitive content
     * @param text The text to check
     * @return true if repetitive content is detected
     */
    private boolean hasRepetitiveContent(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        if (words.length < 4) {
            return false;
        }
        
        // Check for repeated words (more than 3 times)
        java.util.Map<String, Integer> wordCount = new java.util.HashMap<>();
        for (String word : words) {
            if (word.length() > 3) { // Only check meaningful words
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        
        return wordCount.values().stream().anyMatch(count -> count > 3);
    }
    
    /**
     * Result class for meta description validation
     */
    public static class MetaDescriptionValidationResult {
        private boolean valid;
        private java.util.List<String> messages = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public java.util.List<String> getMessages() {
            return messages;
        }
        
        public void addMessage(String message) {
            this.messages.add(message);
        }
        
        public java.util.List<String> getWarnings() {
            return warnings;
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public String getAllMessages() {
            StringBuilder sb = new StringBuilder();
            for (String message : messages) {
                sb.append(message).append("\n");
            }
            for (String warning : warnings) {
                sb.append("Warning: ").append(warning).append("\n");
            }
            return sb.toString().trim();
        }
    }
}