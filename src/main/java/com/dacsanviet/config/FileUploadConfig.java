package com.dacsanviet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file upload handling and static resource serving
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadConfig.class);
    
    @Value("${app.upload.news-images:uploads/news}")
    private String newsImageUploadPath;
    
    @Value("${file.upload-dir:uploads/categories}")
    private String categoryUploadPath;
    
    @Value("${upload.path:uploads/products}")
    private String productUploadPath;
    
    /**
     * Configure multipart file upload settings
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set maximum file size to 5MB
        factory.setMaxFileSize(DataSize.ofMegabytes(5));
        
        // Set maximum request size to 10MB (for multiple files)
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        
        // Set file size threshold for writing to disk
        factory.setFileSizeThreshold(DataSize.ofKilobytes(512));
        
        return factory.createMultipartConfig();
    }
    
    /**
     * Configure static resource handlers for uploaded files
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve news images
        registry.addResourceHandler("/uploads/news/**")
                .addResourceLocations("file:" + newsImageUploadPath + "/")
                .setCachePeriod(3600); // Cache for 1 hour
        
        // Serve category images
        registry.addResourceHandler("/uploads/categories/**")
                .addResourceLocations("file:" + categoryUploadPath + "/")
                .setCachePeriod(3600);
        
        // Serve product images
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadPath + "/")
                .setCachePeriod(3600);
        
        // Serve general uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }
    
    /**
     * Create upload directories on application startup
     */
    @PostConstruct
    public void createUploadDirectories() {
        createDirectoryIfNotExists(newsImageUploadPath, "News images");
        createDirectoryIfNotExists(categoryUploadPath, "Category images");
        createDirectoryIfNotExists(productUploadPath, "Product images");
    }
    
    private void createDirectoryIfNotExists(String path, String description) {
        try {
            Path directory = Paths.get(path);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.info("Created {} upload directory: {}", description, directory.toAbsolutePath());
            } else {
                logger.info("{} upload directory already exists: {}", description, directory.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create {} upload directory: {}", description, path, e);
        }
    }
}