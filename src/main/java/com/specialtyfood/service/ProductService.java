package com.specialtyfood.service;

import com.specialtyfood.dao.*;
import com.specialtyfood.dto.CreateProductRequest;
import com.specialtyfood.dto.UpdateProductRequest;
import com.specialtyfood.model.Category;
import com.specialtyfood.model.Product;
import com.specialtyfood.repository.CategoryRepository;
import com.specialtyfood.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product service for product management operations
 */
@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    private static final String UPLOAD_DIR = "uploads/products/";
    
    /**
     * Get all active products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrueOrderByName(pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get product by ID
     */
    @Cacheable(value = "productDetails", key = "#id")
    @Transactional(readOnly = true)
    public ProductDao getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(product);
    }
    
    /**
     * Search products by keyword
     */
    @Cacheable(value = "searchResults", key = "#keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProductDao> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(pageable);
        }
        
        Page<Product> products = productRepository.searchProducts(keyword.trim(), pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Search products by keyword and category
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> searchProducts(String keyword, Long categoryId, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (categoryId != null) {
                return getProductsByCategory(categoryId, pageable);
            }
            return getAllProducts(pageable);
        }
        
        Page<Product> products = productRepository.searchProductsInCategory(keyword.trim(), categoryId, pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get featured products
     */
    @Cacheable(value = "featuredProducts", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProductDao> getFeaturedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc(pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get products in stock
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getInStockProducts(Pageable pageable) {
        Page<Product> products = productRepository.findInStockProducts(pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get recent products
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getRecentProducts(Pageable pageable) {
        Page<Product> products = productRepository.findRecentProducts(pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Create a new product
     */
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "featuredProducts", allEntries = true),
        @CacheEvict(value = "searchResults", allEntries = true)
    })
    public ProductDao createProduct(CreateProductRequest request) {
        // Check if product name already exists
        if (productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Product with name '" + request.getName() + "' already exists!");
        }
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        // Create new product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        product.setWeightGrams(request.getWeightGrams());
        product.setOrigin(request.getOrigin());
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }
    
    /**
     * Update an existing product
     */
    @Caching(evict = {
        @CacheEvict(value = "productDetails", key = "#id"),
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "featuredProducts", allEntries = true),
        @CacheEvict(value = "searchResults", allEntries = true)
    })
    public ProductDao updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Check if new name is taken by another product
        if (!product.getName().equals(request.getName()) && 
            productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Product with name '" + request.getName() + "' already exists!");
        }
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        // Update product fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setIsActive(request.getIsActive());
        product.setIsFeatured(request.getIsFeatured());
        product.setWeightGrams(request.getWeightGrams());
        product.setOrigin(request.getOrigin());
        product.setCategory(category);
        
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }
    
    /**
     * Delete a product (soft delete by setting isActive to false)
     */
    @Caching(evict = {
        @CacheEvict(value = "productDetails", key = "#id"),
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "featuredProducts", allEntries = true),
        @CacheEvict(value = "searchResults", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    /**
     * Toggle product active status
     */
    public ProductDao toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsActive(!product.getIsActive());
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }
    
    /**
     * Toggle product featured status
     */
    public ProductDao toggleFeaturedStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsFeatured(!product.getIsFeatured());
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }
    
    /**
     * Update product stock
     */
    public ProductDao updateStock(Long id, Integer newStock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (newStock < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }
        
        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }
    
    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<ProductDao> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get out of stock products
     */
    @Transactional(readOnly = true)
    public List<ProductDao> getOutOfStockProducts() {
        List<Product> products = productRepository.findOutOfStockProducts();
        return products.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Upload product image
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL
        return "/uploads/products/" + uniqueFilename;
    }
    
    /**
     * Get similar products (for recommendations)
     */
    @Transactional(readOnly = true)
    public List<ProductDao> getSimilarProducts(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // First try to find products with similar names
        String[] keywords = product.getName().split("\\s+");
        if (keywords.length > 0) {
            List<Product> similarProducts = productRepository.findSimilarProducts(productId, keywords[0], pageable);
            if (!similarProducts.isEmpty()) {
                return similarProducts.stream().map(this::convertToDto).toList();
            }
        }
        
        // If no similar names found, get products from same category
        List<Product> categoryProducts = productRepository.findProductsInSameCategory(
                productId, product.getCategory().getId(), pageable);
        return categoryProducts.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Check if product exists by name
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
    
    /**
     * Convert Product entity to ProductDao
     */
    private ProductDao convertToDto(Product product) {
        CategoryDao CategoryDao = null;
        Long categoryId = null;
        String categoryName = null;
        
        if (product.getCategory() != null) {
            CategoryDao = new CategoryDao(
                product.getCategory().getId(),
                product.getCategory().getName()
            );
            categoryId = product.getCategory().getId();
            categoryName = product.getCategory().getName();
        }
        
        ProductDao dto = new ProductDao(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getImageUrl(),
            product.getIsActive(),
            product.getIsFeatured(),
            product.getWeightGrams(),
            product.getOrigin(),
            CategoryDao,
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
        
        // Set additional fields
        dto.setCategoryId(categoryId);
        dto.setCategoryName(categoryName);
        
        return dto;
    }
    
    /**
     * Get all products for admin (including inactive) with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDao> getAllProductsAdmin(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::convertToDto);
    }
    
    /**
     * Get product by ID as entity (not DTO)
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Product> getProductByIdEntity(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Save product
     */
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "featuredProducts", allEntries = true),
        @CacheEvict(value = "searchResults", allEntries = true),
        @CacheEvict(value = "productDetails", key = "#product.id", condition = "#product.id != null")
    })
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
