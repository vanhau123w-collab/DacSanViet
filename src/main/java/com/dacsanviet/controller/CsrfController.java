package com.dacsanviet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for providing CSRF tokens to frontend forms
 * Enables secure form submissions with CSRF protection
 */
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {
    
    /**
     * Get CSRF token for form submissions
     * 
     * @param request HTTP request containing CSRF token
     * @return CSRF token information
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        Map<String, Object> response = new HashMap<>();
        if (csrfToken != null) {
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
        }
        
        return ResponseEntity.ok(response);
    }
}