package com.specialtyfood.controller;

import com.specialtyfood.dao.CategoryDao;
import com.specialtyfood.dto.CreateCategoryRequest;
import com.specialtyfood.dto.UpdateCategoryRequest;
import com.specialtyfood.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Admin Category Controller for category management
 */
@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * List all categories (admin view)
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryDao> categories;
        
        if (search != null && !search.trim().isEmpty()) {
            categories = categoryService.searchCategories(search, pageable);
            model.addAttribute("search", search);
        } else {
            categories = categoryService.getAllCategories(pageable);
        }
        
        // Get categories with product count for better display
        Page<CategoryDao> categoriesWithCount = categories.map(category -> {
            Long productCount = categoryService.countProductsInCategory(category.getId());
            category.setProductCount(productCount);
            return category;
        });
        
        model.addAttribute("categories", categoriesWithCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("totalElements", categories.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("pageTitle", "Quản Lý Danh Mục");
        
        return "admin/categories/simple-list";
    }
    
    /**
     * Show category details
     */
    @GetMapping("/{id}")
    public String viewCategory(@PathVariable Long id, Model model) {
        try {
            CategoryDao category = categoryService.getCategoryById(id);
            Long productCount = categoryService.countProductsInCategory(id);
            
            model.addAttribute("category", category);
            model.addAttribute("productCount", productCount);
            model.addAttribute("pageTitle", "Chi Tiết Danh Mục - " + category.getName());
            return "admin/categories/view";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy danh mục");
            return "redirect:/admin/categories";
        }
    }
    
    /**
     * Show create category form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CreateCategoryRequest());
        model.addAttribute("pageTitle", "Thêm Danh Mục Mới");
        return "admin/categories/simple-create";
    }
    
    /**
     * Handle create category form submission
     */
    @PostMapping("/create")
    public String createCategory(
            @Valid @ModelAttribute("category") CreateCategoryRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm Danh Mục Mới");
            return "admin/categories/simple-create";
        }
        
        try {
            CategoryDao createdCategory = categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("message", 
                "Danh mục '" + createdCategory.getName() + "' đã được tạo thành công!");
            
            return "redirect:/admin/categories";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Lỗi tạo danh mục: " + e.getMessage());
            model.addAttribute("pageTitle", "Thêm Danh Mục Mới");
            return "admin/categories/simple-create";
        }
    }
    
    /**
     * Show edit category form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            CategoryDao category = categoryService.getCategoryById(id);
            
            // Convert CategoryDao to UpdateCategoryRequest
            UpdateCategoryRequest updateRequest = new UpdateCategoryRequest();
            updateRequest.setName(category.getName());
            updateRequest.setDescription(category.getDescription());
            updateRequest.setIsActive(category.getIsActive());
            
            model.addAttribute("category", updateRequest);
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục - " + category.getName());
            
            return "admin/categories/simple-edit";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy danh mục");
            return "redirect:/admin/categories";
        }
    }
    
    /**
     * Handle edit category form submission
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") UpdateCategoryRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
            return "admin/categories/simple-edit";
        }
        
        try {
            CategoryDao updatedCategory = categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("message", 
                "Danh mục '" + updatedCategory.getName() + "' đã được cập nhật thành công!");
            
            return "redirect:/admin/categories";
            
        } catch (RuntimeException e) {
            model.addAttribute("categoryId", id);
            model.addAttribute("error", "Lỗi cập nhật danh mục: " + e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
            return "admin/categories/simple-edit";
        }
    }
    
    /**
     * Delete category (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryDao category = categoryService.getCategoryById(id);
            categoryService.deleteCategory(id);
            
            redirectAttributes.addFlashAttribute("message", 
                "Danh mục '" + category.getName() + "' đã được xóa thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa danh mục: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    /**
     * Toggle category active status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryDao category = categoryService.toggleCategoryStatus(id);
            String status = category.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            
            redirectAttributes.addFlashAttribute("message", 
                "Danh mục '" + category.getName() + "' đã được " + status + " thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi thay đổi trạng thái danh mục: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
}