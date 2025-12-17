package com.specialtyfood.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2-200 ký tự")
    private String name;
    
    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String description;
    
    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;
    
    @Size(max = 500, message = "URL hình ảnh không được quá 500 ký tự")
    private String imageUrl;
    
    private Boolean isActive = true;
    private Boolean isFeatured = false;
    
    @Min(value = 0, message = "Trọng lượng không được âm")
    private Integer weightGrams;
    
    @Size(max = 100, message = "Xuất xứ không được quá 100 ký tự")
    private String origin;
    
    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;
}