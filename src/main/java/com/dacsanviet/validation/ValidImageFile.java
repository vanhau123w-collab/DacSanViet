package com.dacsanviet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for image file uploads
 * Validates file type, size, and content
 */
@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageFile {
    
    String message() default "File hình ảnh không hợp lệ";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    long maxSize() default 5 * 1024 * 1024; // 5MB default
    
    String[] allowedTypes() default {"image/jpeg", "image/png", "image/webp"};
    
    String[] allowedExtensions() default {"jpg", "jpeg", "png", "webp"};
    
    boolean required() default false;
}