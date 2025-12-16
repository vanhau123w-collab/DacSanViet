package com.specialtyfood.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for masking sensitive data in logs and displays
 */
@Service
public class DataMaskingService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b\\d{4}[-.\\s]?\\d{4}[-.\\s]?\\d{4}[-.\\s]?\\d{4}\\b");
    
    /**
     * Mask email address (show first 2 chars and domain)
     */
    public String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return "****";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "****@" + email.substring(atIndex + 1);
        }
        
        return email.substring(0, 2) + "****@" + email.substring(atIndex + 1);
    }
    
    /**
     * Mask phone number (show last 4 digits)
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        
        String digits = phoneNumber.replaceAll("[^\\d]", "");
        if (digits.length() < 4) {
            return "****";
        }
        
        return "****-****-" + digits.substring(digits.length() - 4);
    }
    
    /**
     * Mask credit card number (show last 4 digits)
     */
    public String maskCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String digits = cardNumber.replaceAll("[^\\d]", "");
        if (digits.length() < 4) {
            return "****";
        }
        
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }
    
    /**
     * Mask general sensitive data (show first and last 2 characters)
     */
    public String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
    
    /**
     * Mask address (show only city and state/country)
     */
    public String maskAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "****";
        }
        
        // Simple masking - in production, you might want more sophisticated parsing
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            // Show last two parts (typically city, state/country)
            return "****, " + parts[parts.length - 2].trim() + ", " + parts[parts.length - 1].trim();
        }
        
        return "****";
    }
    
    /**
     * Mask all sensitive data in a text string
     */
    public String maskAllSensitiveData(String text) {
        if (text == null) {
            return null;
        }
        
        String maskedText = text;
        
        // Mask email addresses
        maskedText = EMAIL_PATTERN.matcher(maskedText).replaceAll(match -> {
            String email = match.group();
            return maskEmail(email);
        });
        
        // Mask phone numbers
        maskedText = PHONE_PATTERN.matcher(maskedText).replaceAll(match -> {
            String phone = match.group();
            return maskPhoneNumber(phone);
        });
        
        // Mask credit card numbers
        maskedText = CREDIT_CARD_PATTERN.matcher(maskedText).replaceAll(match -> {
            String card = match.group();
            return maskCreditCard(card);
        });
        
        return maskedText;
    }
    
    /**
     * Check if data appears to be sensitive
     */
    public boolean isSensitiveData(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("password") ||
               lowerFieldName.contains("card") ||
               lowerFieldName.contains("cvv") ||
               lowerFieldName.contains("ssn") ||
               lowerFieldName.contains("social") ||
               lowerFieldName.contains("tax") ||
               lowerFieldName.contains("account") ||
               lowerFieldName.contains("routing") ||
               lowerFieldName.contains("pin") ||
               lowerFieldName.contains("secret") ||
               lowerFieldName.contains("token") ||
               lowerFieldName.contains("key");
    }
    
    /**
     * Sanitize data for logging
     */
    public String sanitizeForLogging(String fieldName, Object value) {
        if (value == null) {
            return "null";
        }
        
        String stringValue = value.toString();
        
        if (isSensitiveData(fieldName)) {
            return maskSensitiveData(stringValue);
        }
        
        // Check if the value itself looks sensitive
        if (EMAIL_PATTERN.matcher(stringValue).find() ||
            PHONE_PATTERN.matcher(stringValue).find() ||
            CREDIT_CARD_PATTERN.matcher(stringValue).find()) {
            return maskAllSensitiveData(stringValue);
        }
        
        return stringValue;
    }
}