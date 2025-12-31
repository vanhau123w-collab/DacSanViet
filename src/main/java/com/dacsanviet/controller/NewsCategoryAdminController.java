package com.dacsanviet.controller;

import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.service.NewsCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin REST API Controller for News Category Management
 * Provides CRUD operations, search, and filtering for news categories
 * Requires ADMIN or STAFF role for access
 */
@RestController
@RequestMapping("/api/admin/news/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Slf4j
public class NewsCategoryAdminController {
    
    private final NewsCategoryService newsCategoryService;
    
    /**
     * Get all categories with pagination, sorting, and filtering
     * 
     * @param page Page number (0-based)
     * @param size Page size (default 10)
     * @param sort Sort field (default: sortOrder)
     * @param direction Sort direction (default: asc)
     * @param search Search term for name/description
     * @param active Filter by active status
     * @return Paginated list of categories
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active) {
        
        try {
            log.info("Getting categories - page: {}, size: {}, active: {}, search: {}", 
                    page, size, active, search);
            
            // Create sort object
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sortObj = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // Get categories based on filters
            Page<NewsCategoryDto> categories;
            if (search != null && !search.trim().isEmpty()) {
                categories = newsCategoryService.searchCategories(search.trim(), pageable);
            } else if (active != null) {
                categories = newsCategoryService.findByActiveStatus(active, pageable);
            } else {
                categories = newsCategoryService.findAllCategories(pageable);
            }
            
            // Create response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("content", categories.getContent());
            response.put("currentPage", categories.getNumber());
            response.put("totalPages", categories.getTotalPages());
            response.put("totalElements", categories.getTotalElements());
            response.put("size", categories.getSize());
            response.put("first", categories.isFirst());
            response.put("last", categories.isLast());
            response.put("empty", categories.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh sách danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Get all active categories (for dropdowns)
     * 
     * @return List of active categories
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCategories() {
        try {
            log.info("Getting active categories");
            
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error getting active categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh mục hoạt động: " + e.getMessage()));
        }
    }
    
    /**
     * Get categories with article count
     * 
     * @return List of categories with article counts
     */
    @GetMapping("/with-counts")
    public ResponseEntity<?> getCategoriesWithCounts() {
        try {
            log.info("Getting categories with article counts");
            
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategoriesWithArticleCount();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error getting categories with counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh mục với số lượng bài viết: " + e.getMessage()));
        }
    }
    
    /**
     * Get category by ID
     * 
     * @param id Category ID
     * @return Category details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        try {
            log.info("Getting category with id: {}", id);
            
            Optional<NewsCategoryDto> category = newsCategoryService.findById(id);
            if (category.isPresent()) {
                return ResponseEntity.ok(category.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Create new category
     * 
     * @param categoryDto Category data
     * @param bindingResult Validation results
     * @return Created category
     */
    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody NewsCategoryDto categoryDto,
            BindingResult bindingResult) {
        
        try {
            log.info("Creating new category with name: {}", categoryDto.getName());
            
            // Check validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(createValidationErrorResponse(bindingResult));
            }
            
            // Create category
            NewsCategoryDto createdCategory = newsCategoryService.createCategory(categoryDto);
            
            log.info("Created category with id: {}", createdCategory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for category creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tạo danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Update existing category
     * 
     * @param id Category ID
     * @param categoryDto Updated category data
     * @param bindingResult Validation results
     * @return Updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody NewsCategoryDto categoryDto,
            BindingResult bindingResult) {
        
        try {
            log.info("Updating category with id: {}", id);
            
            // Check validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(createValidationErrorResponse(bindingResult));
            }
            
            // Update category
            NewsCategoryDto updatedCategory = newsCategoryService.updateCategory(id, categoryDto);
            
            log.info("Updated category with id: {}", id);
            return ResponseEntity.ok(updatedCategory);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for category update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Delete category (only if it has no articles)
     * 
     * @param id Category ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            log.info("Deleting category with id: {}", id);
            
            newsCategoryService.deleteCategory(id);
            
            log.info("Deleted category with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được xóa thành công"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for deletion: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete category with articles: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi xóa danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Activate category
     * 
     * @param id Category ID
     * @return Success message
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateCategory(@PathVariable Long id) {
        try {
            log.info("Activating category with id: {}", id);
            
            newsCategoryService.activateCategory(id);
            
            log.info("Activated category with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được kích hoạt"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for activation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error activating category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi kích hoạt danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Deactivate category
     * 
     * @param id Category ID
     * @return Success message
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCategory(@PathVariable Long id) {
        try {
            log.info("Deactivating category with id: {}", id);
            
            newsCategoryService.deactivateCategory(id);
            
            log.info("Deactivated category with id: {}", id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được vô hiệu hóa"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for deactivation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deactivating category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi vô hiệu hóa danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Update sort order for category
     * 
     * @param id Category ID
     * @param sortOrder New sort order
     * @return Success message
     */
    @PutMapping("/{id}/sort-order")
    public ResponseEntity<?> updateSortOrder(
            @PathVariable Long id,
            @RequestParam Integer sortOrder) {
        
        try {
            log.info("Updating sort order for category {} to {}", id, sortOrder);
            
            if (sortOrder < 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Thứ tự sắp xếp phải >= 0"));
            }
            
            newsCategoryService.updateSortOrder(id, sortOrder);
            
            log.info("Updated sort order for category {} to {}", id, sortOrder);
            return ResponseEntity.ok(createSuccessResponse("Thứ tự sắp xếp đã được cập nhật"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for sort order update: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating sort order for category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi cập nhật thứ tự sắp xếp: " + e.getMessage()));
        }
    }
    
    /**
     * Move category up in sort order
     * 
     * @param id Category ID
     * @return Success message
     */
    @PostMapping("/{id}/move-up")
    public ResponseEntity<?> moveCategoryUp(@PathVariable Long id) {
        try {
            log.info("Moving category {} up", id);
            
            newsCategoryService.moveCategoryUp(id);
            
            log.info("Moved category {} up", id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được di chuyển lên"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for move up: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error moving category {} up: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi di chuyển danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Move category down in sort order
     * 
     * @param id Category ID
     * @return Success message
     */
    @PostMapping("/{id}/move-down")
    public ResponseEntity<?> moveCategoryDown(@PathVariable Long id) {
        try {
            log.info("Moving category {} down", id);
            
            newsCategoryService.moveCategoryDown(id);
            
            log.info("Moved category {} down", id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được di chuyển xuống"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Category not found for move down: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error moving category {} down: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi di chuyển danh mục: " + e.getMessage()));
        }
    }
    
    /**
     * Get empty categories (categories with no articles)
     * 
     * @return List of empty categories
     */
    @GetMapping("/empty")
    public ResponseEntity<?> getEmptyCategories() {
        try {
            log.info("Getting empty categories");
            
            List<NewsCategoryDto> emptyCategories = newsCategoryService.findEmptyCategories();
            return ResponseEntity.ok(emptyCategories);
            
        } catch (Exception e) {
            log.error("Error getting empty categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh mục trống: " + e.getMessage()));
        }
    }
    
    /**
     * Get categories ordered by article count
     * 
     * @return List of categories ordered by article count (descending)
     */
    @GetMapping("/by-article-count")
    public ResponseEntity<?> getCategoriesByArticleCount() {
        try {
            log.info("Getting categories ordered by article count");
            
            List<NewsCategoryDto> categories = newsCategoryService.findCategoriesOrderedByArticleCount();
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error getting categories by article count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Lỗi khi tải danh mục theo số bài viết: " + e.getMessage()));
        }
    }
    
    // Helper methods for response creation
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Dữ liệu không hợp lệ");
        
        Map<String, String> fieldErrors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage()));
        
        response.put("fieldErrors", fieldErrors);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}