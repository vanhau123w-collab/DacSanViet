package com.specialtyfood.service;

import com.specialtyfood.dto.AddToCartRequest;
import com.specialtyfood.dao.CartDao;
import com.specialtyfood.dao.CartItemDao;
import com.specialtyfood.dao.ProductDao;
import com.specialtyfood.dao.CategoryDao;
import com.specialtyfood.dto.UpdateCartItemRequest;
import com.specialtyfood.model.CartItem;
import com.specialtyfood.model.Product;
import com.specialtyfood.model.User;
import com.specialtyfood.repository.CartItemRepository;
import com.specialtyfood.repository.ProductRepository;
import com.specialtyfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for shopping cart operations
 */
@Service
@Transactional
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public CartService(CartItemRepository cartItemRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Add item to cart or update quantity if item already exists
     */
    @CacheEvict(value = "userCarts", key = "#userId")
    public CartDao addToCart(Long userId, AddToCartRequest request) {
        User user = getUserById(userId);
        Product product = getProductById(request.getProductId());
        
        // Validate product availability
        validateProductAvailability(product, request.getQuantity());
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        
        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Validate new total quantity
            validateProductAvailability(product, newQuantity);
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem(user, product, request.getQuantity());
            cartItemRepository.save(cartItem);
        }
        
        return getCart(userId);
    }
    
    /**
     * Update cart item quantity
     */
    @CacheEvict(value = "userCarts", key = "#userId")
    public CartDao updateCartItem(Long userId, UpdateCartItemRequest request) {
        User user = getUserById(userId);
        Product product = getProductById(request.getProductId());
        
        // Validate product availability
        validateProductAvailability(product, request.getQuantity());
        
        // Find existing cart item
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(product.getPrice()); // Update price in case it changed
        cartItemRepository.save(cartItem);
        
        return getCart(userId);
    }
    
    /**
     * Remove item from cart
     */
    @CacheEvict(value = "userCarts", key = "#userId")
    public CartDao removeFromCart(Long userId, Long productId) {
        getUserById(userId); // Validate user exists
        
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        
        return getCart(userId);
    }
    
    /**
     * Get user's cart
     */
    @Cacheable(value = "userCarts", key = "#userId")
    @Transactional(readOnly = true)
    public CartDao getCart(Long userId) {
        getUserById(userId); // Validate user exists
        
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedDateDesc(userId);
        
        return convertToCartDto(userId, cartItems);
    }
    
    /**
     * Clear user's cart
     */
    @CacheEvict(value = "userCarts", key = "#userId")
    public void clearCart(Long userId) {
        getUserById(userId); // Validate user exists
        
        cartItemRepository.deleteByUserId(userId);
    }
    
    /**
     * Get cart item count for user
     */
    @Transactional(readOnly = true)
    public Long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
    
    /**
     * Get cart total for user
     */
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {
        return cartItemRepository.calculateCartTotal(userId);
    }
    
    /**
     * Remove items with insufficient stock from cart
     */
    public CartDao removeUnavailableItems(Long userId) {
        List<CartItem> unavailableItems = cartItemRepository.findCartItemsWithInsufficientStockByUserId(userId);
        unavailableItems.addAll(cartItemRepository.findCartItemsWithInactiveProductsByUserId(userId));
        
        for (CartItem item : unavailableItems) {
            cartItemRepository.delete(item);
        }
        
        return getCart(userId);
    }
    
    /**
     * Validate cart before checkout
     */
    @Transactional(readOnly = true)
    public boolean validateCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedDateDesc(userId);
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            // Check if product is active
            if (!product.getIsActive()) {
                return false;
            }
            
            // Check if sufficient stock is available
            if (product.getStockQuantity() < item.getQuantity()) {
                return false;
            }
        }
        
        return true;
    }
    
    // Helper methods
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    
    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }
    
    private void validateProductAvailability(Product product, Integer requestedQuantity) {
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available: " + product.getName());
        }
        
        if (product.getStockQuantity() < requestedQuantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + 
                                     ", Requested: " + requestedQuantity);
        }
    }
    
    private CartDao convertToCartDto(Long userId, List<CartItem> cartItems) {
        CartDao CartDao = new CartDao(userId);
        
        for (CartItem item : cartItems) {
            CartDao.addItem(convertToCartItemDto(item));
        }
        
        return CartDao;
    }
    
    private CartItemDao convertToCartItemDto(CartItem cartItem) {
        ProductDao ProductDao = convertToProductDto(cartItem.getProduct());
        
        return new CartItemDao(
                cartItem.getId(),
                ProductDao,
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getAddedDate(),
                cartItem.getUpdatedAt()
        );
    }
    
    private ProductDao convertToProductDto(Product product) {
        CategoryDao CategoryDao = null;
        if (product.getCategory() != null) {
            CategoryDao = new CategoryDao(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            );
            CategoryDao.setDescription(product.getCategory().getDescription());
        }
        
        return new ProductDao(
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
    }
}