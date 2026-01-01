package com.dacsanviet.controller;

import com.dacsanviet.model.Category;
import com.dacsanviet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Global Controller Advice to add common data to all views
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Add categories to all views for header navigation
     */
    @ModelAttribute
    public void addCategories(Model model) {
        try {
            // Get root categories with children eagerly loaded using JOIN FETCH
            List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
            
            // Get all active categories for other uses
            List<Category> allCategories = categoryRepository.findByIsActiveOrderByName(true);
            
            model.addAttribute("headerCategories", rootCategories);
            model.addAttribute("allCategories", allCategories);
        } catch (Exception e) {
            // Fail silently - don't break the page if categories can't be loaded
            System.err.println("Error loading categories for header: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
