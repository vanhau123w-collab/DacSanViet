package com.dacsanviet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Validator for image file uploads
 * Performs comprehensive security validation including file type, size, and content verification
 */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {
    
    private long maxSize;
    private List<String> allowedTypes;
    private List<String> allowedExtensions;
    private boolean required;
    
    // Magic bytes for common image formats
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] WEBP_MAGIC = {0x52, 0x49, 0x46, 0x46}; // "RIFF"
    
    @Override
    public void initialize(ValidImageFile constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = Arrays.asList(constraintAnnotation.allowedTypes());
        this.allowedExtensions = Arrays.asList(constraintAnnotation.allowedExtensions());
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Handle null/empty files
        if (file == null || file.isEmpty()) {
            if (required) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("File hình ảnh là bắt buộc")
                       .addConstraintViolation();
                return false;
            }
            return true;
        }
        
        // Validate file size
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Kích thước file không được vượt quá " + (maxSize / 1024 / 1024) + "MB")
                   .addConstraintViolation();
            return false;
        }
        
        // Validate filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Tên file không hợp lệ")
                   .addConstraintViolation();
            return false;
        }
        
        // Check for dangerous filename patterns
        if (containsDangerousPatterns(originalFilename)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Tên file chứa ký tự không được phép")
                   .addConstraintViolation();
            return false;
        }
        
        // Validate file extension
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + String.join(", ", allowedExtensions))
                   .addConstraintViolation();
            return false;
        }
        
        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Loại file không được hỗ trợ")
                   .addConstraintViolation();
            return false;
        }
        
        // Validate file content (magic bytes)
        try {
            byte[] fileBytes = file.getBytes();
            if (!isValidImageContent(fileBytes, extension)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Nội dung file không phải là hình ảnh hợp lệ")
                       .addConstraintViolation();
                return false;
            }
            
            // Additional validation: try to read as image
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            if (image == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("File không thể đọc được như một hình ảnh")
                       .addConstraintViolation();
                return false;
            }
            
            // Validate image dimensions (reasonable limits)
            if (image.getWidth() > 10000 || image.getHeight() > 10000) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Kích thước hình ảnh quá lớn (tối đa 10000x10000 pixels)")
                       .addConstraintViolation();
                return false;
            }
            
            if (image.getWidth() < 10 || image.getHeight() < 10) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Kích thước hình ảnh quá nhỏ (tối thiểu 10x10 pixels)")
                       .addConstraintViolation();
                return false;
            }
            
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Lỗi khi xử lý file hình ảnh")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
    
    /**
     * Check for dangerous filename patterns that could be used for attacks
     */
    private boolean containsDangerousPatterns(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        // Check for path traversal attempts
        if (lowerFilename.contains("../") || lowerFilename.contains("..\\")) {
            return true;
        }
        
        // Check for null bytes
        if (filename.contains("\0")) {
            return true;
        }
        
        // Check for executable extensions (double extension attacks)
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar", ".php", ".asp", ".jsp"};
        for (String ext : dangerousExtensions) {
            if (lowerFilename.contains(ext)) {
                return true;
            }
        }
        
        // Check for control characters
        for (char c : filename.toCharArray()) {
            if (Character.isISOControl(c) && c != '\t' && c != '\n' && c != '\r') {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate file content by checking magic bytes
     */
    private boolean isValidImageContent(byte[] fileBytes, String extension) {
        if (fileBytes.length < 8) {
            return false;
        }
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return startsWith(fileBytes, JPEG_MAGIC);
            case "png":
                return startsWith(fileBytes, PNG_MAGIC);
            case "webp":
                return startsWith(fileBytes, WEBP_MAGIC) && 
                       fileBytes.length > 12 && 
                       fileBytes[8] == 'W' && fileBytes[9] == 'E' && 
                       fileBytes[10] == 'B' && fileBytes[11] == 'P';
            default:
                return false;
        }
    }
    
    /**
     * Check if byte array starts with given magic bytes
     */
    private boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        
        return true;
    }
}