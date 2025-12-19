package com.dacsanviet.controller;

import com.dacsanviet.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global Controller Advice to add common attributes to all views
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Add categories to all views for header dropdown
     */
    @ModelAttribute
    public void addCategoriesToModel(Model model) {
        try {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
        } catch (Exception e) {
            // Silently fail if categories cannot be loaded
            // This prevents breaking pages if category service is unavailable
        }
    }
}
