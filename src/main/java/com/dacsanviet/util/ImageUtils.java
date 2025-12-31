package com.dacsanviet.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for image processing and validation
 */
public class ImageUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);
    
    // Supported image formats
    public static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");
    
    // MIME types for supported formats
    public static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    
    /**
     * Check if the file is a valid image format
     */
    public static boolean isValidImageFormat(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return SUPPORTED_FORMATS.contains(extension);
    }
    
    /**
     * Check if the MIME type is supported
     */
    public static boolean isValidImageMimeType(String mimeType) {
        return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
    
    /**
     * Generate a safe filename by removing special characters
     */
    public static String generateSafeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "image";
        }
        
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        
        // Remove special characters and replace with underscore
        String safeName = baseName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        
        // Limit length
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }
        
        return safeName + "." + extension;
    }
    
    /**
     * Validate image file dimensions
     */
    public static boolean validateImageDimensions(MultipartFile file, int maxWidth, int maxHeight) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return false;
            }
            
            return image.getWidth() <= maxWidth && image.getHeight() <= maxHeight;
            
        } catch (IOException e) {
            logger.error("Error reading image dimensions", e);
            return false;
        }
    }
    
    /**
     * Get image dimensions as a formatted string
     */
    public static String getImageDimensions(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return "Unknown";
            }
            
            return image.getWidth() + "x" + image.getHeight();
            
        } catch (IOException e) {
            logger.error("Error reading image dimensions", e);
            return "Unknown";
        }
    }
    
    /**
     * Format file size in human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Check if image is in landscape orientation
     */
    public static boolean isLandscape(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return false;
            }
            
            return image.getWidth() > image.getHeight();
            
        } catch (IOException e) {
            logger.error("Error checking image orientation", e);
            return false;
        }
    }
    
    /**
     * Calculate aspect ratio
     */
    public static double getAspectRatio(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return 1.0;
            }
            
            return (double) image.getWidth() / image.getHeight();
            
        } catch (IOException e) {
            logger.error("Error calculating aspect ratio", e);
            return 1.0;
        }
    }
}