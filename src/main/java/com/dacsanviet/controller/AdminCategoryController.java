package com.dacsanviet.controller;

import com.dacsanviet.model.Category;
import com.dacsanviet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Category Management Controller
 */
@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Value("${file.upload-dir:uploads/categories}")
    private String uploadDir;

    /**
     * Categories Management Page
     */
    @GetMapping
    public String categoriesPage(Model model) {
        model.addAttribute("pageTitle", "Categories Management");
        model.addAttribute("activePage", "categories");
        return "admin/categories/index";
    }
    
    /**
     * Create Category Page
     */
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("activePage", "categories");
        return "admin/categories/create";
    }
    
    /**
     * Edit Category Page
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        return categoryRepository.findById(id)
            .map(category -> {
                model.addAttribute("category", category);
                model.addAttribute("activePage", "categories");
                return "admin/categories/edit";
            })
            .orElse("redirect:/admin/categories");
    }

    /**
     * Get all categories with pagination
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<Category> categoriesPage;
            
            if (search != null && !search.trim().isEmpty()) {
                categoriesPage = categoryRepository.searchCategories(search, pageable);
            } else if (isActive != null) {
                categoriesPage = categoryRepository.findByIsActiveOrderByName(isActive).stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(
                            list, pageable, 
                            categoryRepository.findByIsActiveOrderByName(isActive).size()
                        )
                    ));
            } else {
                categoriesPage = categoryRepository.findAll(pageable);
            }
            
            // Add product count for each category
            java.util.List<Map<String, Object>> categoryList = categoriesPage.getContent().stream()
                .map(category -> {
                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("id", category.getId());
                    categoryData.put("name", category.getName());
                    categoryData.put("description", category.getDescription());
                    categoryData.put("imageUrl", category.getImageUrl());
                    categoryData.put("isActive", category.getIsActive());
                    categoryData.put("createdAt", category.getCreatedAt());
                    categoryData.put("updatedAt", category.getUpdatedAt());
                    
                    // Count products
                    Long productCount = categoryRepository.countActiveProductsInCategory(category.getId());
                    categoryData.put("productCount", productCount);
                    
                    return categoryData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", categoryList);
            response.put("currentPage", categoriesPage.getNumber());
            response.put("totalPages", categoriesPage.getTotalPages());
            response.put("totalElements", categoriesPage.getTotalElements());
            response.put("size", categoriesPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading categories: " + e.getMessage());
        }
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(category -> {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("id", category.getId());
                categoryData.put("name", category.getName());
                categoryData.put("description", category.getDescription());
                categoryData.put("imageUrl", category.getImageUrl());
                categoryData.put("isActive", category.getIsActive());
                categoryData.put("productCount", categoryRepository.countActiveProductsInCategory(id));
                return ResponseEntity.ok(categoryData);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new category with multipart upload
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model) {
        
        try {
            // Check if category name already exists
            if (categoryRepository.existsByName(name)) {
                model.addAttribute("error", "Tên danh mục đã tồn tại");
                return "admin/categories/create";
            }
            
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setIsActive(true);
            
            // Handle image upload
            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                category.setImageUrl(imageUrl);
            }
            
            categoryRepository.save(category);
            return "redirect:/admin/categories?success=created";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tạo danh mục: " + e.getMessage());
            return "admin/categories/create";
        }
    }

    /**
     * Update category with multipart upload
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "active", required = false) Boolean active,
            Model model) {
        
        return categoryRepository.findById(id)
            .map(category -> {
                try {
                    // Check if name is being changed and if new name already exists
                    if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
                        model.addAttribute("error", "Tên danh mục đã tồn tại");
                        model.addAttribute("category", category);
                        return "admin/categories/edit";
                    }
                    
                    category.setName(name);
                    category.setDescription(description);
                    category.setIsActive(active != null && active);
                    
                    // Handle image upload
                    if (image != null && !image.isEmpty()) {
                        String imageUrl = saveImage(image);
                        category.setImageUrl(imageUrl);
                    }
                    
                    categoryRepository.save(category);
                    return "redirect:/admin/categories?success=updated";
                } catch (Exception e) {
                    model.addAttribute("error", "Lỗi khi cập nhật danh mục: " + e.getMessage());
                    model.addAttribute("category", category);
                    return "admin/categories/edit";
                }
            })
            .orElse("redirect:/admin/categories");
    }

    /**
     * Delete category
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(category -> {
                try {
                    // Check if category has products
                    Long productCount = categoryRepository.countActiveProductsInCategory(id);
                    if (productCount > 0) {
                        return ResponseEntity.badRequest()
                            .body("Cannot delete category with " + productCount + " active products");
                    }
                    
                    categoryRepository.delete(category);
                    return ResponseEntity.ok().body("Category deleted successfully");
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Error deleting category: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Toggle category active status
     */
    @PatchMapping("/{id}/toggle-active")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(category -> {
                category.setIsActive(!category.getIsActive());
                categoryRepository.save(category);
                return ResponseEntity.ok().body("Category status updated");
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Save uploaded image to disk
     */
    private String saveImage(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadDirPath = Paths.get(uploadDir);
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadDirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL
        return "/uploads/categories/" + filename;
    }
}
