package com.dacsanviet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for HTML content
 * Sanitizes HTML and ensures it doesn't contain dangerous elements
 */
public class HtmlValidator implements ConstraintValidator<ValidHtml, String> {
    
    // Allowed HTML tags for news content
    private static final List<String> ALLOWED_TAGS = Arrays.asList(
        "p", "br", "strong", "b", "em", "i", "u", "h1", "h2", "h3", "h4", "h5", "h6",
        "ul", "ol", "li", "blockquote", "a", "img", "div", "span", "table", "tr", "td", "th",
        "thead", "tbody", "tfoot", "pre", "code"
    );
    
    // Dangerous tags that should never be allowed
    private static final List<String> DANGEROUS_TAGS = Arrays.asList(
        "script", "iframe", "object", "embed", "form", "input", "button", "select", 
        "textarea", "link", "meta", "style", "base", "applet"
    );
    
    private boolean allowEmpty;
    private int maxLength;
    
    @Override
    public void initialize(ValidHtml constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.maxLength = constraintAnnotation.maxLength();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowEmpty;
        }
        
        // Check length
        if (value.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nội dung không được vượt quá " + maxLength + " ký tự")
                   .addConstraintViolation();
            return false;
        }
        
        try {
            // Parse HTML
            Document doc = Jsoup.parse(value);
            
            // Check for dangerous tags
            for (String dangerousTag : DANGEROUS_TAGS) {
                if (!doc.select(dangerousTag).isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Nội dung chứa thẻ không được phép: " + dangerousTag)
                           .addConstraintViolation();
                    return false;
                }
            }
            
            // Check for javascript: URLs
            if (value.toLowerCase().contains("javascript:")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Nội dung chứa JavaScript không được phép")
                       .addConstraintViolation();
                return false;
            }
            
            // Check for on* event handlers
            if (value.toLowerCase().matches(".*\\son\\w+\\s*=.*")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Nội dung chứa event handler không được phép")
                       .addConstraintViolation();
                return false;
            }
            
            // Validate that cleaned HTML is not too different from original
            Safelist safelist = Safelist.relaxed()
                .addTags(ALLOWED_TAGS.toArray(new String[0]))
                .addAttributes("a", "href", "title")
                .addAttributes("img", "src", "alt", "title", "width", "height")
                .addAttributes("blockquote", "cite")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https");
            
            String cleanHtml = Jsoup.clean(value, safelist);
            
            // If cleaned version is significantly shorter, it means dangerous content was removed
            if (cleanHtml.length() < value.length() * 0.8) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Nội dung chứa các thẻ hoặc thuộc tính không được phép")
                       .addConstraintViolation();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nội dung HTML không hợp lệ")
                   .addConstraintViolation();
            return false;
        }
    }
}