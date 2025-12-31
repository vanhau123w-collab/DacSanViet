package com.dacsanviet.service;

import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.model.NewsCategory;
import com.dacsanviet.repository.NewsCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing news categories
 * Handles CRUD operations, search, and business logic for categories
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsCategoryService {
    
    private final NewsCategoryRepository newsCategoryRepository;
    
    // CRUD Operations
    
    /**
     * Create a new news category
     */
    public NewsCategoryDto createCategory(NewsCategoryDto categoryDto) {
        log.info("Creating new category with name: {}", categoryDto.getName());
        
        // Check if category name already exists
        if (newsCategoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        // Generate unique slug
        String slug = generateUniqueSlug(categoryDto.getName());
        
        // Create category entity
        NewsCategory category = new NewsCategory();
        category.setName(categoryDto.getName());
        category.setSlug(slug);
        category.setDescription(categoryDto.getDescription());
        category.setIsActive(categoryDto.getIsActive() != null ? categoryDto.getIsActive() : true);
        category.setSortOrder(categoryDto.getSortOrder() != null ? categoryDto.getSortOrder() : getNextSortOrder());
        
        NewsCategory savedCategory = newsCategoryRepository.save(category);
        log.info("Created category with id: {} and slug: {}", savedCategory.getId(), savedCategory.getSlug());
        
        return convertToDto(savedCategory);
    }
    
    /**
     * Update an existing news category
     */
    public NewsCategoryDto updateCategory(Long id, NewsCategoryDto categoryDto) {
        log.info("Updating category with id: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        // Check if new name conflicts with existing categories (excluding current one)
        if (!category.getName().equals(categoryDto.getName()) && 
            newsCategoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        // Update slug if name changed
        if (!category.getName().equals(categoryDto.getName())) {
            String newSlug = generateUniqueSlug(categoryDto.getName(), id);
            category.setSlug(newSlug);
        }
        
        // Update fields
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setIsActive(categoryDto.getIsActive() != null ? categoryDto.getIsActive() : true);
        category.setSortOrder(categoryDto.getSortOrder() != null ? categoryDto.getSortOrder() : category.getSortOrder());
        
        NewsCategory savedCategory = newsCategoryRepository.save(category);
        log.info("Updated category with id: {}", savedCategory.getId());
        
        return convertToDto(savedCategory);
    }
    
    /**
     * Delete a category (only if it has no articles)
     */
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        // Check if category has articles
        Long articleCount = newsCategoryRepository.countArticlesInCategory(id);
        if (articleCount > 0) {
            throw new IllegalStateException("Cannot delete category with " + articleCount + " articles. Move articles to another category first.");
        }
        
        newsCategoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }
    
    /**
     * Deactivate a category (soft delete)
     */
    public void deactivateCategory(Long id) {
        log.info("Deactivating category with id: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setIsActive(false);
        newsCategoryRepository.save(category);
        
        log.info("Deactivated category with id: {}", id);
    }
    
    /**
     * Activate a category
     */
    public void activateCategory(Long id) {
        log.info("Activating category with id: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setIsActive(true);
        newsCategoryRepository.save(category);
        
        log.info("Activated category with id: {}", id);
    }
    
    /**
     * Find category by ID
     */
    @Transactional(readOnly = true)
    public Optional<NewsCategoryDto> findById(Long id) {
        return newsCategoryRepository.findById(id)
            .map(this::convertToDto);
    }
    
    /**
     * Find category by slug
     */
    @Transactional(readOnly = true)
    public Optional<NewsCategoryDto> findBySlug(String slug) {
        return newsCategoryRepository.findBySlug(slug)
            .map(this::convertToDto);
    }
    
    /**
     * Find category by name
     */
    @Transactional(readOnly = true)
    public Optional<NewsCategoryDto> findByName(String name) {
        return newsCategoryRepository.findByName(name)
            .map(this::convertToDto);
    }
    
    // Query Operations
    
    /**
     * Find all active categories ordered by sort order
     */
    @Transactional(readOnly = true)
    public List<NewsCategoryDto> findActiveCategories() {
        return newsCategoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Find all categories with pagination
     */
    @Transactional(readOnly = true)
    public Page<NewsCategoryDto> findAllCategories(Pageable pageable) {
        return newsCategoryRepository.findAll(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find categories by active status
     */
    @Transactional(readOnly = true)
    public Page<NewsCategoryDto> findByActiveStatus(Boolean isActive, Pageable pageable) {
        return newsCategoryRepository.findByIsActive(isActive, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Search categories by keyword
     */
    @Transactional(readOnly = true)
    public Page<NewsCategoryDto> searchCategories(String keyword, Pageable pageable) {
        return newsCategoryRepository.searchCategories(keyword, pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find all categories for admin (including inactive ones)
     */
    @Transactional(readOnly = true)
    public Page<NewsCategoryDto> findAllForAdmin(Pageable pageable) {
        return newsCategoryRepository.findAll(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Find categories with published articles
     */
    @Transactional(readOnly = true)
    public List<NewsCategoryDto> findCategoriesWithPublishedArticles() {
        return newsCategoryRepository.findCategoriesWithPublishedArticles()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Find active categories with article count
     */
    @Transactional(readOnly = true)
    public List<NewsCategoryDto> findActiveCategoriesWithArticleCount() {
        List<Object[]> results = newsCategoryRepository.findActiveCategoriesWithPublishedArticleCount();
        return results.stream()
            .map(result -> {
                NewsCategory category = (NewsCategory) result[0];
                Long articleCount = (Long) result[1];
                NewsCategoryDto dto = convertToDto(category);
                dto.setArticleCount(articleCount);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Find categories ordered by article count
     */
    @Transactional(readOnly = true)
    public List<NewsCategoryDto> findCategoriesOrderedByArticleCount() {
        return newsCategoryRepository.findActiveCategoriesOrderedByPublishedArticleCount()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Find empty categories (no articles)
     */
    @Transactional(readOnly = true)
    public List<NewsCategoryDto> findEmptyCategories() {
        return newsCategoryRepository.findEmptyCategories()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    // Sort Order Management
    
    /**
     * Update sort order for a category
     */
    public void updateSortOrder(Long id, Integer newSortOrder) {
        log.info("Updating sort order for category id: {} to {}", id, newSortOrder);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setSortOrder(newSortOrder);
        newsCategoryRepository.save(category);
        
        log.info("Updated sort order for category id: {} to {}", id, newSortOrder);
    }
    
    /**
     * Move category up in sort order
     */
    public void moveCategoryUp(Long id) {
        log.info("Moving category up: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        if (category.getSortOrder() > 0) {
            category.setSortOrder(category.getSortOrder() - 1);
            newsCategoryRepository.save(category);
            log.info("Moved category up: {}", id);
        }
    }
    
    /**
     * Move category down in sort order
     */
    public void moveCategoryDown(Long id) {
        log.info("Moving category down: {}", id);
        
        NewsCategory category = newsCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        Integer maxSortOrder = newsCategoryRepository.findMaxSortOrder();
        if (category.getSortOrder() < maxSortOrder) {
            category.setSortOrder(category.getSortOrder() + 1);
            newsCategoryRepository.save(category);
            log.info("Moved category down: {}", id);
        }
    }
    
    // Helper Methods
    
    /**
     * Get next available sort order
     */
    private Integer getNextSortOrder() {
        Integer maxSortOrder = newsCategoryRepository.findMaxSortOrder();
        return maxSortOrder != null ? maxSortOrder + 1 : 0;
    }
    
    /**
     * Generate unique slug from name
     */
    private String generateUniqueSlug(String name) {
        return generateUniqueSlug(name, null);
    }
    
    /**
     * Generate unique slug from name, excluding specific category ID
     */
    private String generateUniqueSlug(String name, Long excludeId) {
        String baseSlug = createSlugFromName(name);
        String slug = baseSlug;
        int counter = 1;
        
        while (isSlugExists(slug, excludeId)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    /**
     * Create slug from name
     */
    private String createSlugFromName(String name) {
        return name.toLowerCase()
            .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
            .replaceAll("[èéẹẻẽêềếệểễ]", "e")
            .replaceAll("[ìíịỉĩ]", "i")
            .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
            .replaceAll("[ùúụủũưừứựửữ]", "u")
            .replaceAll("[ỳýỵỷỹ]", "y")
            .replaceAll("[đ]", "d")
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
    
    /**
     * Check if slug exists, excluding specific category ID
     */
    private boolean isSlugExists(String slug, Long excludeId) {
        Optional<NewsCategory> existing = newsCategoryRepository.findBySlug(slug);
        return existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId));
    }
    
    /**
     * Convert NewsCategory entity to DTO
     */
    private NewsCategoryDto convertToDto(NewsCategory category) {
        NewsCategoryDto dto = new NewsCategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Get article count if not already set
        if (dto.getArticleCount() == null) {
            Long articleCount = newsCategoryRepository.countPublishedArticlesInCategory(category.getId());
            dto.setArticleCount(articleCount);
        }
        
        return dto;
    }
}