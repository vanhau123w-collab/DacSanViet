package com.specialtyfood.service;

import com.specialtyfood.dto.CategoryDto;
import com.specialtyfood.dto.CreateCategoryRequest;
import com.specialtyfood.dto.UpdateCategoryRequest;
import com.specialtyfood.model.Category;
import com.specialtyfood.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Category service for category management operations
 */
@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Get all active categories
     */
    @Cacheable(value = "categories", key = "'active'")
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByName();
        return categories.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get all categories with pagination
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(this::convertToDto);
    }
    
    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToDto(category);
    }
    
    /**
     * Get category by name
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
        return convertToDto(category);
    }
    
    /**
     * Search categories by keyword
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories(pageable);
        }
        
        Page<Category> categories = categoryRepository.searchCategories(keyword.trim(), pageable);
        return categories.map(this::convertToDto);
    }
    
    /**
     * Get categories with active products
     */
    @Cacheable(value = "categories", key = "'withProducts'")
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesWithProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithActiveProducts();
        return categories.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get categories with product count
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesWithProductCount() {
        List<Object[]> results = categoryRepository.findCategoriesWithProductCount();
        return results.stream().map(result -> {
            Category category = (Category) result[0];
            Long productCount = (Long) result[1];
            
            CategoryDto dto = convertToDto(category);
            dto.setProductCount(productCount);
            return dto;
        }).toList();
    }
    
    /**
     * Get categories ordered by product count
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesOrderedByProductCount() {
        List<Category> categories = categoryRepository.findCategoriesOrderedByProductCount();
        return categories.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get empty categories (no products)
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getEmptyCategories() {
        List<Category> categories = categoryRepository.findEmptyCategories();
        return categories.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Create a new category
     */
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto createCategory(CreateCategoryRequest request) {
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists!");
        }
        
        // Create new category
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    
    /**
     * Update an existing category
     */
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if new name is taken by another category
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists!");
        }
        
        // Update category fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToDto(updatedCategory);
    }
    
    /**
     * Delete a category (soft delete by setting isActive to false)
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if category has active products
        Long productCount = categoryRepository.countActiveProductsInCategory(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with active products. " +
                    "Please move or deactivate all products first.");
        }
        
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    /**
     * Toggle category active status
     */
    public CategoryDto toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);
        return convertToDto(updatedCategory);
    }
    
    /**
     * Count products in category
     */
    @Transactional(readOnly = true)
    public Long countProductsInCategory(Long categoryId) {
        return categoryRepository.countActiveProductsInCategory(categoryId);
    }
    
    /**
     * Check if category exists by name
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    /**
     * Convert Category entity to CategoryDto
     */
    private CategoryDto convertToDto(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIsActive(),
            null, // productCount will be set separately when needed
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}