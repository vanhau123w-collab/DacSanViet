package com.specialtyfood.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data
 */
@Service
public class EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${app.encryption.key:defaultEncryptionKeyThatShouldBeChangedInProduction}")
    private String encryptionKey;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Encrypt sensitive data
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            logger.error("Error encrypting data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Error decrypting data", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Hash sensitive data (one-way)
     */
    public String hash(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            // Use SHA-256 for hashing
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (Exception e) {
            logger.error("Error hashing data", e);
            throw new RuntimeException("Failed to hash data", e);
        }
    }
    
    /**
     * Generate a secure random key for encryption
     */
    public String generateSecureKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating secure key", e);
            throw new RuntimeException("Failed to generate secure key", e);
        }
    }
    
    /**
     * Mask sensitive data for logging (show only first and last 2 characters)
     */
    public String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
    
    /**
     * Get secret key from configuration
     */
    private SecretKey getSecretKey() {
        try {
            // In production, this should be loaded from a secure key management system
            byte[] keyBytes;
            
            if (encryptionKey.length() == 44) { // Base64 encoded 32-byte key
                keyBytes = Base64.getDecoder().decode(encryptionKey);
            } else {
                // Derive key from string (not recommended for production)
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            }
            
            return new SecretKeySpec(keyBytes, ALGORITHM);
            
        } catch (Exception e) {
            logger.error("Error creating secret key", e);
            throw new RuntimeException("Failed to create secret key", e);
        }
    }
}