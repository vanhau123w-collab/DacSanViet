package com.dacsanviet.controller;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.service.NewsService;
import com.dacsanviet.service.NewsCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Admin Controller for News Management Templates
 * Serves HTML pages for news administration
 * Requires ADMIN or STAFF role for access
 */
@Controller
@RequestMapping("/admin/news")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService newsService;
    private final NewsCategoryService categoryService;

    /**
     * Show news management index page
     */
    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) NewsStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            Model model) {
        
        try {
            // Create pageable with sorting by creation date descending
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            // Get articles based on filters
            Page<NewsArticleDto> articles;
            if (search != null && !search.trim().isEmpty()) {
                articles = newsService.searchArticles(search.trim(), pageable);
            } else {
                articles = newsService.findAllForAdmin(status, pageable);
            }
            
            // Get active categories for filter dropdown
            List<NewsCategoryDto> categories = categoryService.findActiveCategories();
            
            // Add attributes to model
            model.addAttribute("articles", articles);
            model.addAttribute("categories", categories);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentCategoryId", categoryId);
            model.addAttribute("currentSearch", search);
            model.addAttribute("pageTitle", "Quản Lý Tin Tức");
            model.addAttribute("activePage", "news");
            
            return "admin/news/index";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách tin tức: " + e.getMessage());
            return "admin/news/index";
        }
    }

    /**
     * Show create news article form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        try {
            // Get active categories for dropdown
            List<NewsCategoryDto> categories = categoryService.findActiveCategories();
            
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Tạo Tin Tức Mới");
            model.addAttribute("activePage", "news");
            
            return "admin/news/create";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải form tạo tin tức: " + e.getMessage());
            return "redirect:/admin/news";
        }
    }

    /**
     * Show edit news article form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get article by ID
            Optional<NewsArticleDto> articleOpt = newsService.findById(id);
            if (articleOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tin tức với ID: " + id);
                return "redirect:/admin/news";
            }
            
            NewsArticleDto article = articleOpt.get();
            
            // Get active categories for dropdown
            List<NewsCategoryDto> categories = categoryService.findActiveCategories();
            
            model.addAttribute("article", article);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Chỉnh Sửa Tin Tức");
            model.addAttribute("activePage", "news");
            
            return "admin/news/edit";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải tin tức: " + e.getMessage());
            return "redirect:/admin/news";
        }
    }

    /**
     * Show categories management page
     */
    @GetMapping("/categories")
    public String showCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        try {
            // Create pageable with sorting by sort order then name
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("sortOrder").ascending().and(Sort.by("name").ascending()));
            
            // Get categories
            Page<NewsCategoryDto> categories = categoryService.findAllForAdmin(pageable);
            
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Quản Lý Danh Mục Tin Tức");
            model.addAttribute("activePage", "news");
            
            return "admin/news/categories";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách danh mục: " + e.getMessage());
            return "admin/news/categories";
        }
    }

    /**
     * Show image upload page
     */
    @GetMapping("/images")
    public String showImageUpload(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Hình Ảnh Tin Tức");
        model.addAttribute("activePage", "news");
        return "admin/news/image-upload";
    }

    /**
     * Handle form submissions - redirect to API endpoints
     * These methods redirect to the REST API endpoints for actual processing
     */
    
    @PostMapping("/create")
    public String createArticle(RedirectAttributes redirectAttributes) {
        // This will be handled by JavaScript calling the REST API
        // Redirect back to create form with message
        redirectAttributes.addFlashAttribute("info", "Vui lòng sử dụng form để tạo tin tức");
        return "redirect:/admin/news/create";
    }
    
    @PostMapping("/edit/{id}")
    public String updateArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // This will be handled by JavaScript calling the REST API
        // Redirect back to edit form with message
        redirectAttributes.addFlashAttribute("info", "Vui lòng sử dụng form để cập nhật tin tức");
        return "redirect:/admin/news/edit/" + id;
    }
    
    @PostMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tin tức thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa tin tức: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }
}