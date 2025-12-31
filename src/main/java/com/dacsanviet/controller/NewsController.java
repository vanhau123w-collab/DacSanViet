package com.dacsanviet.controller;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.dto.NewsCommentDto;
import com.dacsanviet.service.NewsService;
import com.dacsanviet.service.NewsCategoryService;
import com.dacsanviet.service.NewsCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Public News Controller for displaying news articles to users
 * Handles news listing, article detail view, category filtering, and search
 */
@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    
    private final NewsService newsService;
    private final NewsCategoryService newsCategoryService;
    private final NewsCommentService newsCommentService;
    
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int FEATURED_ARTICLES_LIMIT = 3;
    private static final int RECENT_ARTICLES_LIMIT = 6;
    
    /**
     * Display news listing page with pagination
     * Requirements: 2.1, 2.4, 2.5
     */
    @GetMapping
    public String listNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        try {
            log.info("Displaying news listing - page: {}, size: {}", page, size);
            
            // Validate page size (max 24 articles per page)
            if (size > 24) {
                size = 24;
            }
            
            // Create pageable with sort by published date desc
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "publishedAt"));
            
            // Get published articles
            Page<NewsArticleDto> articles = newsService.findPublishedArticles(pageable);
            
            // Get featured articles for hero section
            List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(FEATURED_ARTICLES_LIMIT);
            
            // Get active categories for navigation
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            
            // Get recent articles for sidebar
            List<NewsArticleDto> recentArticles = newsService.findRecentArticles(RECENT_ARTICLES_LIMIT);
            
            // Add data to model
            model.addAttribute("articles", articles);
            model.addAttribute("featuredArticles", featuredArticles);
            model.addAttribute("categories", categories);
            model.addAttribute("recentArticles", recentArticles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", articles.getTotalPages());
            model.addAttribute("totalElements", articles.getTotalElements());
            model.addAttribute("pageTitle", "Tin Tức - Đặc Sản Việt");
            model.addAttribute("pageDescription", "Cập nhật những tin tức mới nhất về đặc sản Việt Nam");
            
            log.info("Loaded {} articles for news listing", articles.getNumberOfElements());
            return "news/news-list";
            
        } catch (Exception e) {
            log.error("Error loading news listing: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Không thể tải danh sách tin tức. Vui lòng thử lại sau.");
            return "error/500";
        }
    }
    
    /**
     * Display article detail page and increment view count
     * Requirements: 2.2
     */
    @GetMapping("/{slug}")
    public String viewArticle(@PathVariable String slug, Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Viewing article with slug: {}", slug);
            
            // Find article by slug
            Optional<NewsArticleDto> articleOpt = newsService.findBySlug(slug);
            
            if (articleOpt.isEmpty()) {
                log.warn("Article not found with slug: {}", slug);
                redirectAttributes.addFlashAttribute("errorMessage", "Bài viết không tồn tại hoặc đã bị xóa.");
                return "redirect:/news";
            }
            
            NewsArticleDto article = articleOpt.get();
            
            // Increment view count
            newsService.incrementViewCount(article.getId());
            
            // Get related articles from same category
            List<NewsArticleDto> relatedArticles = null;
            if (article.getCategoryId() != null) {
                Pageable pageable = PageRequest.of(0, 4, 
                    Sort.by(Sort.Direction.DESC, "publishedAt"));
                Page<NewsArticleDto> relatedPage = newsService.findByCategory(article.getCategoryId(), pageable);
                relatedArticles = relatedPage.getContent().stream()
                    .filter(a -> !a.getId().equals(article.getId())) // Exclude current article
                    .limit(3)
                    .toList();
            }
            
            // Get active categories for navigation
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            
            // Get recent articles for sidebar
            List<NewsArticleDto> recentArticles = newsService.findRecentArticles(RECENT_ARTICLES_LIMIT);
            
            // Get approved comments for this article
            List<NewsCommentDto> comments = newsCommentService.findApprovedCommentsByArticle(article.getId());
            
            // Create empty comment DTO for form
            NewsCommentDto commentDto = new NewsCommentDto();
            commentDto.setArticleId(article.getId());
            
            // Add data to model
            model.addAttribute("article", article);
            model.addAttribute("relatedArticles", relatedArticles);
            model.addAttribute("categories", categories);
            model.addAttribute("recentArticles", recentArticles);
            model.addAttribute("comments", comments);
            model.addAttribute("commentDto", commentDto);
            model.addAttribute("pageTitle", article.getTitle() + " - Đặc Sản Việt");
            model.addAttribute("pageDescription", article.getMetaDescription() != null ? 
                article.getMetaDescription() : article.getExcerpt());
            model.addAttribute("pageKeywords", article.getMetaKeywords());
            
            log.info("Loaded article: {} (ID: {})", article.getTitle(), article.getId());
            return "news/article-detail";
            
        } catch (Exception e) {
            log.error("Error loading article with slug {}: {}", slug, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải bài viết. Vui lòng thử lại sau.");
            return "redirect:/news";
        }
    }
    
    /**
     * Display articles by category with pagination
     * Requirements: 3.1
     */
    @GetMapping("/category/{categorySlug}")
    public String listByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Displaying articles for category: {} - page: {}, size: {}", categorySlug, page, size);
            
            // Find category by slug
            Optional<NewsCategoryDto> categoryOpt = newsCategoryService.findBySlug(categorySlug);
            
            if (categoryOpt.isEmpty()) {
                log.warn("Category not found with slug: {}", categorySlug);
                redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không tồn tại.");
                return "redirect:/news";
            }
            
            NewsCategoryDto category = categoryOpt.get();
            
            // Validate page size
            if (size > 24) {
                size = 24;
            }
            
            // Create pageable with sort by published date desc
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "publishedAt"));
            
            // Get articles by category
            Page<NewsArticleDto> articles = newsService.findByCategorySlug(categorySlug, pageable);
            
            // Get active categories for navigation
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            
            // Get recent articles for sidebar
            List<NewsArticleDto> recentArticles = newsService.findRecentArticles(RECENT_ARTICLES_LIMIT);
            
            // Add data to model
            model.addAttribute("articles", articles);
            model.addAttribute("currentCategory", category);
            model.addAttribute("categories", categories);
            model.addAttribute("recentArticles", recentArticles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", articles.getTotalPages());
            model.addAttribute("totalElements", articles.getTotalElements());
            model.addAttribute("pageTitle", category.getName() + " - Tin Tức - Đặc Sản Việt");
            model.addAttribute("pageDescription", "Tin tức về " + category.getName().toLowerCase());
            
            log.info("Loaded {} articles for category: {}", articles.getNumberOfElements(), category.getName());
            return "news/category";
            
        } catch (Exception e) {
            log.error("Error loading articles for category {}: {}", categorySlug, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải danh sách tin tức. Vui lòng thử lại sau.");
            return "redirect:/news";
        }
    }
    
    /**
     * Search articles by keyword with pagination
     * Requirements: 3.2
     */
    @GetMapping("/search")
    public String searchNews(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Searching articles with keyword: '{}' - page: {}, size: {}", q, page, size);
            
            // Validate search query
            if (q == null || q.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập từ khóa tìm kiếm.");
                return "redirect:/news";
            }
            
            String keyword = q.trim();
            
            // Validate keyword length
            if (keyword.length() < 2) {
                redirectAttributes.addFlashAttribute("errorMessage", "Từ khóa tìm kiếm phải có ít nhất 2 ký tự.");
                return "redirect:/news";
            }
            
            // Validate page size
            if (size > 24) {
                size = 24;
            }
            
            // Create pageable with sort by published date desc
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "publishedAt"));
            
            // Search articles
            Page<NewsArticleDto> articles = newsService.searchArticles(keyword, pageable);
            
            // Get active categories for navigation
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            
            // Get recent articles for sidebar
            List<NewsArticleDto> recentArticles = newsService.findRecentArticles(RECENT_ARTICLES_LIMIT);
            
            // Add data to model
            model.addAttribute("articles", articles);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("categories", categories);
            model.addAttribute("recentArticles", recentArticles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", articles.getTotalPages());
            model.addAttribute("totalElements", articles.getTotalElements());
            model.addAttribute("pageTitle", "Tìm kiếm: " + keyword + " - Tin Tức - Đặc Sản Việt");
            model.addAttribute("pageDescription", "Kết quả tìm kiếm cho từ khóa: " + keyword);
            
            log.info("Found {} articles for keyword: '{}'", articles.getTotalElements(), keyword);
            return "news/search";
            
        } catch (Exception e) {
            log.error("Error searching articles with keyword '{}': {}", q, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thực hiện tìm kiếm. Vui lòng thử lại sau.");
            return "redirect:/news";
        }
    }
    
    /**
     * Display featured articles (for AJAX requests or separate page)
     * Requirements: 2.3
     */
    @GetMapping("/featured")
    public String featuredArticles(Model model) {
        try {
            log.info("Loading featured articles");
            
            // Get featured articles
            List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(10);
            
            // Get active categories for navigation
            List<NewsCategoryDto> categories = newsCategoryService.findActiveCategories();
            
            // Add data to model
            model.addAttribute("articles", featuredArticles);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Tin Nổi Bật - Đặc Sản Việt");
            model.addAttribute("pageDescription", "Những tin tức nổi bật về đặc sản Việt Nam");
            
            log.info("Loaded {} featured articles", featuredArticles.size());
            return "news/featured";
            
        } catch (Exception e) {
            log.error("Error loading featured articles: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Không thể tải tin nổi bật. Vui lòng thử lại sau.");
            return "error/500";
        }
    }
    
    /**
     * API endpoint to get articles for AJAX pagination
     * Requirements: 2.4
     */
    @GetMapping("/api/articles")
    @ResponseBody
    public Page<NewsArticleDto> getArticlesApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        try {
            log.info("API request for articles - page: {}, size: {}, category: {}, search: {}", 
                    page, size, category, search);
            
            // Validate page size
            if (size > 24) {
                size = 24;
            }
            
            // Create pageable
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "publishedAt"));
            
            // Get articles based on filters
            Page<NewsArticleDto> articles;
            
            if (search != null && !search.trim().isEmpty()) {
                articles = newsService.searchArticles(search.trim(), pageable);
            } else if (category != null && !category.trim().isEmpty()) {
                articles = newsService.findByCategorySlug(category.trim(), pageable);
            } else {
                articles = newsService.findPublishedArticles(pageable);
            }
            
            log.info("API returned {} articles", articles.getNumberOfElements());
            return articles;
            
        } catch (Exception e) {
            log.error("Error in articles API: {}", e.getMessage(), e);
            return Page.empty();
        }
    }
}