package com.dacsanviet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Additional security configuration specifically for news management
 * Provides enhanced security headers and content security policies
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class NewsSecurityConfig {
    
    /**
     * Configure security headers for news content
     */
    @Bean
    public org.springframework.security.web.header.HeaderWriter contentSecurityPolicyHeaderWriter() {
        return new org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter(
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
            "img-src 'self' data: https: http:; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "connect-src 'self'; " +
            "media-src 'self'; " +
            "object-src 'none'; " +
            "frame-src 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'"
        );
    }
    
    /**
     * Configure X-XSS-Protection header
     */
    @Bean
    public XXssProtectionHeaderWriter xssProtectionHeaderWriter() {
        return new XXssProtectionHeaderWriter();
    }
    
    /**
     * Configure Referrer Policy header
     */
    @Bean
    public ReferrerPolicyHeaderWriter referrerPolicyHeaderWriter() {
        return new ReferrerPolicyHeaderWriter(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN);
    }
}