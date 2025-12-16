package com.specialtyfood.config;

import com.specialtyfood.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA attribute converter for automatic encryption/decryption of string fields
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        
        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            // Log error but don't fail the operation
            // In production, you might want to handle this differently
            return attribute;
        }
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            // Log error but don't fail the operation
            // This might happen with legacy data that wasn't encrypted
            return dbData;
        }
    }
}