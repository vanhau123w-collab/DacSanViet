package com.dacsanviet.controller;

import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.model.ProductImage;
import com.dacsanviet.service.ProductService;
import com.dacsanviet.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Product controller for public product browsing
 */
@Controller
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * List all products (public view)
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDao> products;
        
        // Always load all products for client-side filtering
        // Only apply search filter on server-side
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, categoryId, pageable);
        } else {
            // Load all products regardless of categoryId - let client-side handle category filtering
            products = productService.getAllProducts(pageable);
        }
        
        model.addAttribute("products", products.getContent());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("searchKeyword", search);
        model.addAttribute("selectedCategory", categoryId != null ? categoryId.toString() : null);
        model.addAttribute("totalElements", products.getTotalElements());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("pageTitle", "Sản Phẩm Đặc Sản");
        
        return "products/index";
    }
    
    /**
     * Show product details
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        try {
            ProductDao product = productService.getProductById(id);
            List<ProductImage> productImages = productService.getProductImages(id);
            
            // Get related products (same category, limit 8)
            Pageable pageable = PageRequest.of(0, 8);
            Page<ProductDao> relatedProducts;
            if (product.getCategoryId() != null) {
                relatedProducts = productService.getProductsByCategory(product.getCategoryId(), pageable);
                // Remove current product from related products
                List<ProductDao> filteredRelated = relatedProducts.getContent().stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(8)
                    .collect(java.util.stream.Collectors.toList());
                model.addAttribute("relatedProducts", filteredRelated);
            } else {
                relatedProducts = productService.getAllProducts(pageable);
                model.addAttribute("relatedProducts", relatedProducts.getContent());
            }
            
            model.addAttribute("product", product);
            model.addAttribute("productImages", productImages);
            model.addAttribute("pageTitle", product.getName());
            return "products/detail";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/products";
        }
    }
}