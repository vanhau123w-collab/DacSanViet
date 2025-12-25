package com.dacsanviet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/products}")
    private String uploadPath;
    
    @Value("${file.upload-dir:uploads/categories}")
    private String categoryUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve product uploaded files
        String uploadLocation = "file:" + Paths.get(uploadPath).toAbsolutePath().toString() + "/";
        
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(3600);
        
        // Serve category uploaded files
        String categoryUploadLocation = "file:" + Paths.get(categoryUploadPath).toAbsolutePath().toString() + "/";
        
        registry.addResourceHandler("/uploads/categories/**")
                .addResourceLocations(categoryUploadLocation)
                .setCachePeriod(3600);
    }
}
