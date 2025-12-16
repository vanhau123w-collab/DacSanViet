package com.specialtyfood.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging security-related events and audit trails
 */
@Service
public class SecurityAuditService {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditService.class);
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    /**
     * Log successful authentication
     */
    public void logSuccessfulAuthentication(String username, String ipAddress, String userAgent) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHENTICATION_SUCCESS");
        auditData.put("username", dataMaskingService.maskSensitiveData(username));
        auditData.put("ipAddress", ipAddress);
        auditData.put("userAgent", userAgent);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.info("Authentication successful: {}", auditData);
    }
    
    /**
     * Log failed authentication attempt
     */
    public void logFailedAuthentication(String username, String ipAddress, String userAgent, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "AUTHENTICATION_FAILURE");
        auditData.put("username", dataMaskingService.maskSensitiveData(username));
        auditData.put("ipAddress", ipAddress);
        auditData.put("userAgent", userAgent);
        auditData.put("reason", reason);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.warn("Authentication failed: {}", auditData);
    }
    
    /**
     * Log password reset request
     */
    public void logPasswordResetRequest(String email, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "PASSWORD_RESET_REQUEST");
        auditData.put("email", dataMaskingService.maskEmail(email));
        auditData.put("ipAddress", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.info("Password reset requested: {}", auditData);
    }
    
    /**
     * Log successful password reset
     */
    public void logPasswordResetSuccess(String email, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "PASSWORD_RESET_SUCCESS");
        auditData.put("email", dataMaskingService.maskEmail(email));
        auditData.put("ipAddress", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.info("Password reset successful: {}", auditData);
    }
    
    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(String username, String resource, String ipAddress, String userAgent) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "UNAUTHORIZED_ACCESS");
        auditData.put("username", username != null ? dataMaskingService.maskSensitiveData(username) : "anonymous");
        auditData.put("resource", resource);
        auditData.put("ipAddress", ipAddress);
        auditData.put("userAgent", userAgent);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.warn("Unauthorized access attempt: {}", auditData);
    }
    
    /**
     * Log admin action
     */
    public void logAdminAction(String adminUsername, String action, String targetResource, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "ADMIN_ACTION");
        auditData.put("adminUsername", dataMaskingService.maskSensitiveData(adminUsername));
        auditData.put("action", action);
        auditData.put("targetResource", targetResource);
        auditData.put("ipAddress", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.info("Admin action performed: {}", auditData);
    }
    
    /**
     * Log data access
     */
    public void logDataAccess(String username, String dataType, String operation, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "DATA_ACCESS");
        auditData.put("username", dataMaskingService.maskSensitiveData(username));
        auditData.put("dataType", dataType);
        auditData.put("operation", operation);
        auditData.put("ipAddress", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.debug("Data access: {}", auditData);
    }
    
    /**
     * Log security configuration change
     */
    public void logSecurityConfigChange(String adminUsername, String configType, String oldValue, String newValue, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "SECURITY_CONFIG_CHANGE");
        auditData.put("adminUsername", dataMaskingService.maskSensitiveData(adminUsername));
        auditData.put("configType", configType);
        auditData.put("oldValue", dataMaskingService.sanitizeForLogging("config", oldValue));
        auditData.put("newValue", dataMaskingService.sanitizeForLogging("config", newValue));
        auditData.put("ipAddress", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.warn("Security configuration changed: {}", auditData);
    }
    
    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String description, String username, String ipAddress, String details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "SUSPICIOUS_ACTIVITY");
        auditData.put("description", description);
        auditData.put("username", username != null ? dataMaskingService.maskSensitiveData(username) : "unknown");
        auditData.put("ipAddress", ipAddress);
        auditData.put("details", dataMaskingService.sanitizeForLogging("details", details));
        auditData.put("timestamp", LocalDateTime.now());
        
        securityLogger.error("Suspicious activity detected: {}", auditData);
    }
    
    /**
     * Extract IP address from HTTP request
     */
    public String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Extract user agent from HTTP request
     */
    public String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }
}