package com.dacsanviet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for URL slugs
 * Ensures slugs follow SEO-friendly format: lowercase letters, numbers, hyphens only
 */
public class SlugValidator implements ConstraintValidator<ValidSlug, String> {
    
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    
    private boolean allowEmpty;
    
    @Override
    public void initialize(ValidSlug constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowEmpty;
        }
        
        // Check length constraints
        if (value.length() < 3 || value.length() > 100) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Slug phải từ 3 đến 100 ký tự")
                   .addConstraintViolation();
            return false;
        }
        
        // Check pattern
        if (!SLUG_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // Additional checks
        if (value.startsWith("-") || value.endsWith("-")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Slug không được bắt đầu hoặc kết thúc bằng dấu gạch ngang")
                   .addConstraintViolation();
            return false;
        }
        
        if (value.contains("--")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Slug không được chứa hai dấu gạch ngang liên tiếp")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}