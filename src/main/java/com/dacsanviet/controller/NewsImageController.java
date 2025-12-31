package com.dacsanviet.controller;

import com.dacsanviet.service.ImageService;
import com.dacsanviet.service.ImageService.ImageUploadResult;
import com.dacsanviet.validation.ValidImageFile;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling news image uploads
 */
@RestController
@RequestMapping("/api/admin/news/images")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Validated
public class NewsImageController {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsImageController.class);
    
    @Autowired
    private ImageService imageService;
    
    /**
     * Upload a single image for news article
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") @ValidImageFile(required = true) MultipartFile file,
            @RequestParam(value = "articleId", required = false) Long articleId) {
        
        try {
            logger.info("Uploading image for article ID: {}", articleId);
            
            ImageUploadResult result = imageService.uploadNewsImage(file, articleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("data", Map.of(
                "originalUrl", result.getOriginalUrl(),
                "thumbnailUrl", result.getThumbnailUrl(),
                "filename", result.getFilename(),
                "fileSize", result.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Image validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi server khi upload hình ảnh"
            ));
        }
    }
    
    /**
     * Upload multiple images for news article
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "articleId", required = false) Long articleId) {
        
        try {
            logger.info("Uploading {} images for article ID: {}", files.length, articleId);
            
            List<ImageUploadResult> results = imageService.uploadMultipleNewsImages(files, articleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Images uploaded successfully");
            response.put("data", results.stream().map(result -> Map.of(
                "originalUrl", result.getOriginalUrl(),
                "thumbnailUrl", result.getThumbnailUrl(),
                "filename", result.getFilename(),
                "fileSize", result.getFileSize()
            )).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to upload multiple images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi server khi upload hình ảnh"
            ));
        }
    }
    
    /**
     * Delete an image
     */
    @DeleteMapping
    public ResponseEntity<?> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            logger.info("Deleting image: {}", imageUrl);
            
            boolean deleted = imageService.deleteImage(imageUrl);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Image deleted successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to delete image"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete image: {}", imageUrl, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi server khi xóa hình ảnh"
            ));
        }
    }
    
    /**
     * Get upload configuration and limits
     */
    @GetMapping("/config")
    public ResponseEntity<?> getUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxFileSize", "5MB");
        config.put("supportedFormats", List.of("JPG", "PNG", "WebP"));
        config.put("maxWidth", 1200);
        config.put("maxHeight", 800);
        config.put("thumbnailWidth", 300);
        config.put("thumbnailHeight", 200);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", config
        ));
    }
}