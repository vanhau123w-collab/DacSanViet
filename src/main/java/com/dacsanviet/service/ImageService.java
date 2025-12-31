package com.dacsanviet.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for handling image upload, validation, and processing for news articles
 */
public interface ImageService {
    
    /**
     * Upload and process a single image for news article
     * @param file The uploaded image file
     * @param articleId The ID of the news article (optional, can be null for new articles)
     * @return ImageUploadResult containing URLs for original and thumbnail
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException if file validation fails
     */
    ImageUploadResult uploadNewsImage(MultipartFile file, Long articleId) throws IOException;
    
    /**
     * Upload multiple images for news article
     * @param files Array of uploaded image files
     * @param articleId The ID of the news article
     * @return List of ImageUploadResult
     * @throws IOException if file processing fails
     */
    List<ImageUploadResult> uploadMultipleNewsImages(MultipartFile[] files, Long articleId) throws IOException;
    
    /**
     * Delete an image and its thumbnail
     * @param imageUrl The URL of the image to delete
     * @return true if deletion was successful
     */
    boolean deleteImage(String imageUrl);
    
    /**
     * Validate image file format and size
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateImageFile(MultipartFile file);
    
    /**
     * Get the storage path for news images
     * @return The base path for news image storage
     */
    String getNewsImageStoragePath();
    
    /**
     * Result class for image upload operations
     */
    class ImageUploadResult {
        private final String originalUrl;
        private final String thumbnailUrl;
        private final String filename;
        private final long fileSize;
        
        public ImageUploadResult(String originalUrl, String thumbnailUrl, String filename, long fileSize) {
            this.originalUrl = originalUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.filename = filename;
            this.fileSize = fileSize;
        }
        
        public String getOriginalUrl() { return originalUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getFilename() { return filename; }
        public long getFileSize() { return fileSize; }
    }
}