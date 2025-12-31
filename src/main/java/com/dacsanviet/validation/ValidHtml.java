package com.dacsanviet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for HTML content
 * Validates that HTML content is safe and doesn't contain malicious scripts
 */
@Documented
@Constraint(validatedBy = HtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHtml {
    
    String message() default "Nội dung HTML chứa thẻ không được phép";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    boolean allowEmpty() default true;
    
    int maxLength() default 50000;
}