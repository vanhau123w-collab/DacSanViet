package com.dacsanviet.controller;

import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.model.Category;
import com.dacsanviet.model.Product;
import com.dacsanviet.model.ProductImage;
import com.dacsanviet.repository.CategoryRepository;
import com.dacsanviet.repository.ProductImageRepository;
import com.dacsanviet.repository.ProductRepository;
import com.dacsanviet.service.CategoryService;
import com.dacsanviet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private com.dacsanviet.repository.SupplierRepository supplierRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryService categoryService;

    @Value("${upload.path:uploads/products}")
    private String uploadPath;

    /**
     * Show create product form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("pageTitle", "Tạo Sản Phẩm Mới");
        model.addAttribute("activePage", "products");
        return "admin/products/create";
    }

    /**
     * Show edit product form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            List<ProductImage> images = productService.getProductImages(id);
            
            model.addAttribute("product", product);
            model.addAttribute("productImages", images);
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
            model.addAttribute("activePage", "products");
            return "admin/products/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/products";
        }
    }

    /**
     * Create product with multipart upload
     */
    @PostMapping("/create")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "story", required = false) String story,
            @RequestParam("price") Double price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            Model model) {
        
        try {
            // Get category entity
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Create product
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setStory(story);
            product.setPrice(BigDecimal.valueOf(price));
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            product.setIsActive(true);
            
            // Set supplier if provided
            if (supplierId != null) {
                com.dacsanviet.model.Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
                product.setSupplier(supplier);
            }
            
            // Handle main image upload
            if (mainImage != null && !mainImage.isEmpty()) {
                String imageUrl = saveImage(mainImage);
                product.setImageUrl(imageUrl);
            }
            
            Product savedProduct = productRepository.save(product);
            
            // Handle additional images
            if (additionalImages != null && additionalImages.length > 0) {
                int displayOrder = 1;
                for (MultipartFile file : additionalImages) {
                    if (!file.isEmpty()) {
                        String imageUrl = saveImage(file);
                        ProductImage productImage = new ProductImage();
                        productImage.setProductId(savedProduct.getId());
                        productImage.setImageUrl(imageUrl);
                        productImage.setDisplayOrder(displayOrder++);
                        productImageRepository.save(productImage);
                    }
                }
            }
            
            return "redirect:/admin/products?success=created";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tạo sản phẩm: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            return "admin/products/create";
        }
    }

    /**
     * Update product with multipart upload
     */
    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "story", required = false) String story,
            @RequestParam("price") Double price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            Model model) {
        
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Get category entity
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            product.setName(name);
            product.setDescription(description);
            product.setStory(story);
            product.setPrice(BigDecimal.valueOf(price));
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            product.setIsActive(active != null && active);
            
            // Set supplier if provided
            if (supplierId != null) {
                com.dacsanviet.model.Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
                product.setSupplier(supplier);
            } else {
                product.setSupplier(null);
            }
            product.setDescription(description);
            product.setStory(story);
            product.setPrice(BigDecimal.valueOf(price));
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            product.setIsActive(active != null ? active : false);
            
            // Handle main image upload
            if (mainImage != null && !mainImage.isEmpty()) {
                String imageUrl = saveImage(mainImage);
                product.setImageUrl(imageUrl);
            }
            
            productRepository.save(product);
            
            // Handle additional images
            if (additionalImages != null && additionalImages.length > 0) {
                List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
                int displayOrder = existingImages.size() + 1;
                
                for (MultipartFile file : additionalImages) {
                    if (!file.isEmpty()) {
                        String imageUrl = saveImage(file);
                        ProductImage productImage = new ProductImage();
                        productImage.setProductId(product.getId());
                        productImage.setImageUrl(imageUrl);
                        productImage.setDisplayOrder(displayOrder++);
                        productImageRepository.save(productImage);
                    }
                }
            }
            
            return "redirect:/admin/products?success=updated";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    /**
     * Upload image API endpoint
     */
    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String imageUrl = saveImage(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete product image (ADMIN only)
     */
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            productImageRepository.deleteById(imageId);
            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Save uploaded image to disk
     */
    private String saveImage(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL
        return "/uploads/products/" + filename;
    }
}
