package com.specialtyfood.controller;

import com.specialtyfood.dto.CreateProductRequest;
import com.specialtyfood.dao.ProductDao;
import com.specialtyfood.dto.UpdateProductRequest;
import com.specialtyfood.model.Category;
import com.specialtyfood.model.Product;
import com.specialtyfood.service.CategoryService;
import com.specialtyfood.service.ProductService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Admin Product Controller for product management
 */
@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * List all products (admin view)
     */
    @GetMapping
    public String listProducts(
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
        Page<ProductDao> products;
        
        if (search != null && !search.trim().isEmpty()) {
            // For admin, we need to search all products including inactive ones
            products = productService.getAllProductsAdmin(pageable);
            // Filter by search term manually since we need all products
            // This is a simple implementation - you might want to create a custom repository method
        } else {
            products = productService.getAllProductsAdmin(pageable);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalElements", products.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Quản Lý Sản Phẩm");
        
        return "admin/products/simple-list";
    }
    
    /**
     * Show product details
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        try {
            ProductDao product = productService.getProductById(id);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Chi Tiết Sản Phẩm - " + product.getName());
            return "admin/products/view";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/admin/products";
        }
    }
    
    /**
     * Show create product form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("product", new CreateProductRequest());
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
        
        return "admin/products/simple-create";
    }
    
    /**
     * Handle create product form submission
     */
    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") CreateProductRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
            return "admin/products/create";
        }
        
        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = productService.uploadProductImage(imageFile);
                request.setImageUrl(imageUrl);
            }
            
            ProductDao createdProduct = productService.createProduct(request);
            redirectAttributes.addFlashAttribute("message", 
                "Sản phẩm '" + createdProduct.getName() + "' đã được tạo thành công!");
            
            return "redirect:/admin/products";
            
        } catch (RuntimeException e) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Lỗi tạo sản phẩm: " + e.getMessage());
            model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
            return "admin/products/simple-create";
        } catch (IOException e) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Lỗi tải lên hình ảnh: " + e.getMessage());
            model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
            return "admin/products/simple-create";
        }
    }
    
    /**
     * Show edit product form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ProductDao product = productService.getProductById(id);
            List<Category> categories = categoryService.getAllCategories();
            
            // Convert ProductDao to UpdateProductRequest
            UpdateProductRequest updateRequest = new UpdateProductRequest();
            updateRequest.setName(product.getName());
            updateRequest.setDescription(product.getDescription());
            updateRequest.setPrice(product.getPrice());
            updateRequest.setStockQuantity(product.getStockQuantity());
            updateRequest.setImageUrl(product.getImageUrl());
            updateRequest.setIsActive(product.getIsActive());
            updateRequest.setIsFeatured(product.getIsFeatured());
            updateRequest.setWeightGrams(product.getWeightGrams());
            updateRequest.setOrigin(product.getOrigin());
            updateRequest.setCategoryId(product.getCategoryId());
            
            model.addAttribute("product", updateRequest);
            model.addAttribute("productId", id);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm - " + product.getName());
            
            return "admin/products/edit";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/admin/products";
        }
    }
    
    /**
     * Handle edit product form submission
     */
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") UpdateProductRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("productId", id);
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            return "admin/products/edit";
        }
        
        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = productService.uploadProductImage(imageFile);
                request.setImageUrl(imageUrl);
            }
            
            ProductDao updatedProduct = productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("message", 
                "Sản phẩm '" + updatedProduct.getName() + "' đã được cập nhật thành công!");
            
            return "redirect:/admin/products";
            
        } catch (RuntimeException e) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("productId", id);
            model.addAttribute("error", "Lỗi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            return "admin/products/edit";
        } catch (IOException e) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("productId", id);
            model.addAttribute("error", "Lỗi tải lên hình ảnh: " + e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            return "admin/products/edit";
        }
    }
    
    /**
     * Delete product (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductDao product = productService.getProductById(id);
            productService.deleteProduct(id);
            
            redirectAttributes.addFlashAttribute("message", 
                "Sản phẩm '" + product.getName() + "' đã được xóa thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa sản phẩm: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * Toggle product active status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleProductStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductDao product = productService.toggleProductStatus(id);
            String status = product.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            
            redirectAttributes.addFlashAttribute("message", 
                "Sản phẩm '" + product.getName() + "' đã được " + status + " thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi thay đổi trạng thái sản phẩm: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * Toggle product featured status
     */
    @PostMapping("/{id}/toggle-featured")
    public String toggleFeaturedStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductDao product = productService.toggleFeaturedStatus(id);
            String status = product.getIsFeatured() ? "nổi bật" : "bình thường";
            
            redirectAttributes.addFlashAttribute("message", 
                "Sản phẩm '" + product.getName() + "' đã được đặt là " + status + " thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi thay đổi trạng thái nổi bật: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * Update product stock
     */
    @PostMapping("/{id}/update-stock")
    public String updateStock(
            @PathVariable Long id, 
            @RequestParam Integer newStock,
            RedirectAttributes redirectAttributes) {
        try {
            ProductDao product = productService.updateStock(id, newStock);
            
            redirectAttributes.addFlashAttribute("message", 
                "Kho của sản phẩm '" + product.getName() + "' đã được cập nhật thành " + newStock + "!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật kho: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * Show low stock products
     */
    @GetMapping("/low-stock")
    public String showLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold,
            Model model) {
        
        List<ProductDao> lowStockProducts = productService.getLowStockProducts(threshold);
        
        model.addAttribute("products", lowStockProducts);
        model.addAttribute("threshold", threshold);
        model.addAttribute("pageTitle", "Sản Phẩm Sắp Hết Hàng");
        
        return "admin/products/low-stock";
    }
    
    /**
     * Show out of stock products
     */
    @GetMapping("/out-of-stock")
    public String showOutOfStockProducts(Model model) {
        List<ProductDao> outOfStockProducts = productService.getOutOfStockProducts();
        
        model.addAttribute("products", outOfStockProducts);
        model.addAttribute("pageTitle", "Sản Phẩm Hết Hàng");
        
        return "admin/products/out-of-stock";
    }
}