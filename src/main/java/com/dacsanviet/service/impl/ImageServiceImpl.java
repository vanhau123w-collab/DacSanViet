package com.dacsanviet.service.impl;

import com.dacsanviet.service.ImageService;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    
    // Supported image formats
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");
    
    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // Thumbnail dimensions
    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 200;
    
    // Optimized image max dimensions
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 800;
    
    @Value("${app.upload.news-images:uploads/news}")
    private String newsImageUploadPath;
    
    @Override
    public ImageUploadResult uploadNewsImage(MultipartFile file, Long articleId) throws IOException {
        logger.info("Starting image upload for article ID: {}", articleId);
        
        // Validate the file
        validateImageFile(file);
        
        // Create directory structure
        Path uploadDir = createDirectoryStructure();
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        String baseFilename = UUID.randomUUID().toString();
        String filename = baseFilename + "." + extension;
        
        // Save original file temporarily
        Path tempFilePath = uploadDir.resolve("temp_" + filename);
        Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        try {
            // Read and process the image
            BufferedImage originalImage = ImageIO.read(tempFilePath.toFile());
            if (originalImage == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }
            
            // Create optimized version
            BufferedImage optimizedImage = optimizeImage(originalImage);
            String optimizedFilename = baseFilename + "_optimized." + extension;
            Path optimizedPath = uploadDir.resolve(optimizedFilename);
            ImageIO.write(optimizedImage, extension, optimizedPath.toFile());
            
            // Create thumbnail
            BufferedImage thumbnail = createThumbnail(originalImage);
            String thumbnailFilename = baseFilename + "_thumb." + extension;
            Path thumbnailPath = uploadDir.resolve(thumbnailFilename);
            ImageIO.write(thumbnail, extension, thumbnailPath.toFile());
            
            // Generate URLs
            String baseUrl = "/uploads/news/" + getCurrentDatePath() + "/";
            String originalUrl = baseUrl + optimizedFilename;
            String thumbnailUrl = baseUrl + thumbnailFilename;
            
            logger.info("Image upload successful. Original: {}, Thumbnail: {}", originalUrl, thumbnailUrl);
            
            return new ImageUploadResult(originalUrl, thumbnailUrl, optimizedFilename, file.getSize());
            
        } finally {
            // Clean up temporary file
            Files.deleteIfExists(tempFilePath);
        }
    }
    
    @Override
    public List<ImageUploadResult> uploadMultipleNewsImages(MultipartFile[] files, Long articleId) throws IOException {
        List<ImageUploadResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    ImageUploadResult result = uploadNewsImage(file, articleId);
                    results.add(result);
                } catch (Exception e) {
                    logger.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                    // Continue with other files, don't fail the entire batch
                }
            }
        }
        
        return results;
    }
    
    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return false;
            }
            
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String baseFilename = FilenameUtils.getBaseName(filename);
            String extension = FilenameUtils.getExtension(filename);
            
            // Construct paths for both original and thumbnail
            Path uploadDir = Paths.get(newsImageUploadPath).resolve(getCurrentDatePath());
            
            // Delete optimized image
            Path optimizedPath = uploadDir.resolve(baseFilename + "_optimized." + extension);
            Files.deleteIfExists(optimizedPath);
            
            // Delete thumbnail
            Path thumbnailPath = uploadDir.resolve(baseFilename + "_thumb." + extension);
            Files.deleteIfExists(thumbnailPath);
            
            logger.info("Successfully deleted image: {}", imageUrl);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to delete image: {}", imageUrl, e);
            return false;
        }
    }
    
    @Override
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }
        
        // Check file format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
        
        // Security: Check for dangerous filename patterns
        if (containsDangerousPatterns(originalFilename)) {
            throw new IllegalArgumentException("Tên file chứa ký tự không được phép");
        }
        
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: JPG, PNG, WebP");
        }
        
        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File không phải là hình ảnh hợp lệ");
        }
        
        // Security: Validate file content by checking magic bytes
        try {
            byte[] fileBytes = file.getBytes();
            if (!isValidImageContent(fileBytes, extension)) {
                throw new IllegalArgumentException("Nội dung file không phải là hình ảnh hợp lệ");
            }
            
            // Additional security: Try to read as image to ensure it's not malicious
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(fileBytes));
            if (image == null) {
                throw new IllegalArgumentException("File không thể đọc được như một hình ảnh");
            }
            
            // Validate reasonable image dimensions
            if (image.getWidth() > 10000 || image.getHeight() > 10000) {
                throw new IllegalArgumentException("Kích thước hình ảnh quá lớn (tối đa 10000x10000 pixels)");
            }
            
            if (image.getWidth() < 10 || image.getHeight() < 10) {
                throw new IllegalArgumentException("Kích thước hình ảnh quá nhỏ (tối thiểu 10x10 pixels)");
            }
            
        } catch (IOException e) {
            throw new IllegalArgumentException("Lỗi khi đọc file hình ảnh: " + e.getMessage());
        }
    }
    
    @Override
    public String getNewsImageStoragePath() {
        return newsImageUploadPath;
    }
    
    /**
     * Create directory structure for storing images
     * Uses date-based folder structure: uploads/news/2024/12/
     */
    private Path createDirectoryStructure() throws IOException {
        Path uploadDir = Paths.get(newsImageUploadPath).resolve(getCurrentDatePath());
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.info("Created directory: {}", uploadDir);
        }
        
        return uploadDir;
    }
    
    /**
     * Get current date path for organizing images by date
     */
    private String getCurrentDatePath() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
    }
    
    /**
     * Optimize image by resizing if necessary and maintaining quality
     */
    private BufferedImage optimizeImage(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // If image is already within limits, return as is
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return original;
        }
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth, newHeight;
        
        if (originalWidth > originalHeight) {
            newWidth = MAX_WIDTH;
            newHeight = (int) (MAX_WIDTH / aspectRatio);
        } else {
            newHeight = MAX_HEIGHT;
            newWidth = (int) (MAX_HEIGHT * aspectRatio);
        }
        
        // Resize using high-quality algorithm
        return Scalr.resize(original, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 
                           newWidth, newHeight, Scalr.OP_ANTIALIAS);
    }
    
    /**
     * Create thumbnail image
     */
    private BufferedImage createThumbnail(BufferedImage original) {
        return Scalr.resize(original, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT,
                           THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Scalr.OP_ANTIALIAS);
    }
    
    /**
     * Security: Check for dangerous filename patterns that could be used for attacks
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
     * Security: Validate file content by checking magic bytes
     */
    private boolean isValidImageContent(byte[] fileBytes, String extension) {
        if (fileBytes.length < 8) {
            return false;
        }
        
        // Magic bytes for common image formats
        byte[] jpegMagic = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        byte[] pngMagic = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] webpMagic = {0x52, 0x49, 0x46, 0x46}; // "RIFF"
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return startsWith(fileBytes, jpegMagic);
            case "png":
                return startsWith(fileBytes, pngMagic);
            case "webp":
                return startsWith(fileBytes, webpMagic) && 
                       fileBytes.length > 12 && 
                       fileBytes[8] == 'W' && fileBytes[9] == 'E' && 
                       fileBytes[10] == 'B' && fileBytes[11] == 'P';
            default:
                return false;
        }
    }
    
    /**
     * Security: Check if byte array starts with given magic bytes
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