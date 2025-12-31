package com.dacsanviet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for NewsCategory entity
 * Used for transferring news category data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCategoryDto {
    
    private Long id;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;
    
    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    private Boolean isActive;
    
    private Integer sortOrder;
    
    private Long articleCount; // Number of published articles in this category
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructor for basic category creation
    public NewsCategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.sortOrder = 0;
        this.articleCount = 0L;
    }
    
    // Constructor for category listing with article count
    public NewsCategoryDto(Long id, String name, String slug, String description, 
                          Boolean isActive, Integer sortOrder, Long articleCount) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.articleCount = articleCount;
    }
}