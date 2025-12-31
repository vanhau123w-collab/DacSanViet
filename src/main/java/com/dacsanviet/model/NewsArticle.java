package com.dacsanviet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * News Article entity representing news articles in the system
 */
@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_news_status", columnList = "status"),
    @Index(name = "idx_news_published_at", columnList = "published_at"),
    @Index(name = "idx_news_category", columnList = "category_id"),
    @Index(name = "idx_news_featured", columnList = "is_featured"),
    @Index(name = "idx_news_slug", columnList = "slug"),
    @Index(name = "idx_news_author", columnList = "author_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 5, max = 200, message = "Tiêu đề phải từ 5 đến 200 ký tự")
    private String title;
    
    @Column(unique = true, nullable = false, length = 250)
    @NotBlank(message = "Slug không được để trống")
    @Size(max = 250, message = "Slug không được vượt quá 250 ký tự")
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 300)
    @Size(max = 300, message = "Tóm tắt không được vượt quá 300 ký tự")
    private String excerpt;
    
    @Column(name = "featured_image", length = 500)
    private String featuredImage;
    
    @Column(name = "thumbnail_image", length = 500)
    private String thumbnailImage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsStatus status = NewsStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NewsCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "meta_description", length = 160)
    @Size(max = 160, message = "Meta description không được vượt quá 160 ký tự")
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 255)
    @Size(max = 255, message = "Meta keywords không được vượt quá 255 ký tự")
    private String metaKeywords;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NewsComment> comments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructor with required fields
    public NewsArticle(String title, String slug, User author) {
        this.title = title;
        this.slug = slug;
        this.author = author;
        this.status = NewsStatus.DRAFT;
        this.viewCount = 0L;
        this.isFeatured = false;
        this.comments = new ArrayList<>();
    }
    
    public NewsArticle(String title, String slug, String content, User author, NewsCategory category) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.author = author;
        this.category = category;
        this.status = NewsStatus.DRAFT;
        this.viewCount = 0L;
        this.isFeatured = false;
        this.comments = new ArrayList<>();
    }
    
    // Helper methods
    public void addComment(NewsComment comment) {
        comments.add(comment);
        comment.setArticle(this);
    }
    
    public void removeComment(NewsComment comment) {
        comments.remove(comment);
        comment.setArticle(null);
    }
    
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1L;
    }
    
    public void publish() {
        this.status = NewsStatus.PUBLISHED;
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
    
    public void unpublish() {
        this.status = NewsStatus.DRAFT;
    }
    
    public void archive() {
        this.status = NewsStatus.ARCHIVED;
    }
    
    public void toggleFeatured() {
        this.isFeatured = !this.isFeatured;
    }
    
    public boolean isPublished() {
        return this.status == NewsStatus.PUBLISHED;
    }
    
    public boolean isDraft() {
        return this.status == NewsStatus.DRAFT;
    }
    
    public boolean isArchived() {
        return this.status == NewsStatus.ARCHIVED;
    }
}