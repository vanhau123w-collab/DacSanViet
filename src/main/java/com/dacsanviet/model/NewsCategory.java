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
 * News Category entity for categorizing news articles
 */
@Entity
@Table(name = "news_categories", indexes = {
    @Index(name = "idx_news_category_name", columnList = "name"),
    @Index(name = "idx_news_category_slug", columnList = "slug"),
    @Index(name = "idx_news_category_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;
    
    @Column(unique = true, length = 120)
    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;
    
    @Column(length = 500)
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NewsArticle> articles = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructor with required fields
    public NewsCategory(String name) {
        this.name = name;
        this.isActive = true;
        this.sortOrder = 0;
        this.articles = new ArrayList<>();
    }
    
    public NewsCategory(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.isActive = true;
        this.sortOrder = 0;
        this.articles = new ArrayList<>();
    }
    
    // Helper methods
    public void addArticle(NewsArticle article) {
        articles.add(article);
        article.setCategory(this);
    }
    
    public void removeArticle(NewsArticle article) {
        articles.remove(article);
        article.setCategory(null);
    }
}