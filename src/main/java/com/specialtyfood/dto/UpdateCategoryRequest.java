package com.specialtyfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2-100 ký tự")
    private String name;
    
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;
    
    private Boolean isActive = true;
}