package com.dacsanviet.dto;

import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.validation.ValidHtml;
import com.dacsanviet.validation.ValidSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for NewsArticle entity
 * Used for transferring news article data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    
    private Long id;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 5, max = 200, message = "Tiêu đề phải từ 5 đến 200 ký tự")
    private String title;
    
    //@ValidSlug(allowEmpty = true)
    private String slug;
    
    //@ValidHtml(maxLength = 50000, allowEmpty = true)
    private String content;
    
    @Size(max = 300, message = "Tóm tắt không được vượt quá 300 ký tự")
    private String excerpt;
    
    private String featuredImage;
    
    private String thumbnailImage;
    
    private NewsStatus status;
    
    private Long categoryId;
    
    private String categoryName;
    
    private String categorySlug;
    
    private Long authorId;
    
    private String authorName;
    
    private Long viewCount;
    
    private Boolean isFeatured;
    
    @Size(max = 160, message = "Meta description không được vượt quá 160 ký tự")
    private String metaDescription;
    
    @Size(max = 255, message = "Meta keywords không được vượt quá 255 ký tự")
    private String metaKeywords;
    
    private LocalDateTime publishedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructor for basic article creation
    public NewsArticleDto(String title, String content, Long categoryId, Long authorId) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.status = NewsStatus.DRAFT;
        this.viewCount = 0L;
        this.isFeatured = false;
    }
    
    // Constructor for article listing
    public NewsArticleDto(Long id, String title, String slug, String excerpt, 
                         String thumbnailImage, NewsStatus status, String categoryName, 
                         String authorName, Long viewCount, Boolean isFeatured, 
                         LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.thumbnailImage = thumbnailImage;
        this.status = status;
        this.categoryName = categoryName;
        this.authorName = authorName;
        this.viewCount = viewCount;
        this.isFeatured = isFeatured;
        this.publishedAt = publishedAt;
    }
}