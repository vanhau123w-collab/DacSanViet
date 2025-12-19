/**
 * Client-side Cart Manager using localStorage
 * Works for both guests and logged-in users
 */

const CartManager = {
    CART_KEY: 'shopping_cart',
    
    /**
     * Get cart from localStorage
     */
    getCart() {
        try {
            const cart = localStorage.getItem(this.CART_KEY);
            return cart ? JSON.parse(cart) : { items: [], total: 0 };
        } catch (e) {
            console.error('Error reading cart:', e);
            return { items: [], total: 0 };
        }
    },
    
    /**
     * Save cart to localStorage
     */
    saveCart(cart) {
        try {
            // Add timestamp to track changes
            cart.lastModified = Date.now();
            localStorage.setItem(this.CART_KEY, JSON.stringify(cart));
            this.updateCartBadge();
            
            // Broadcast change to other tabs/windows
            window.dispatchEvent(new StorageEvent('storage', {
                key: this.CART_KEY,
                newValue: JSON.stringify(cart),
                url: window.location.href
            }));
        } catch (e) {
            console.error('Error saving cart:', e);
        }
    },
    
    /**
     * Add item to cart
     */
    addItem(productId, productName, price, quantity, imageUrl) {
        const cart = this.getCart();
        
        // Check if item already exists
        const existingItem = cart.items.find(item => item.productId === productId);
        
        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            cart.items.push({
                productId,
                productName,
                price,
                quantity,
                imageUrl,
                addedAt: new Date().toISOString()
            });
        }
        
        this.calculateTotal(cart);
        this.saveCart(cart);
        
        return cart;
    },
    
    /**
     * Update item quantity
     */
    updateItem(productId, quantity) {
        const cart = this.getCart();
        const item = cart.items.find(item => item.productId === productId);
        
        if (item) {
            if (quantity <= 0) {
                this.removeItem(productId);
            } else {
                item.quantity = quantity;
                this.calculateTotal(cart);
                this.saveCart(cart);
            }
        }
        
        return cart;
    },
    
    /**
     * Remove item from cart
     */
    removeItem(productId) {
        const cart = this.getCart();
        const oldLength = cart.items.length;
        cart.items = cart.items.filter(item => item.productId !== productId);
        this.calculateTotal(cart);
        this.saveCart(cart);
        
        // Trigger custom event for same-window updates
        if (oldLength !== cart.items.length) {
            window.dispatchEvent(new CustomEvent('cartItemRemoved', { 
                detail: { productId, cart } 
            }));
        }
        
        return cart;
    },
    
    /**
     * Clear cart
     */
    clearCart() {
        localStorage.removeItem(this.CART_KEY);
        this.updateCartBadge();
    },
    
    /**
     * Get cart item count
     */
    getItemCount() {
        const cart = this.getCart();
        return cart.items.reduce((total, item) => total + item.quantity, 0);
    },
    
    /**
     * Calculate cart total
     */
    calculateTotal(cart) {
        cart.total = cart.items.reduce((sum, item) => {
            return sum + (item.price * item.quantity);
        }, 0);
        return cart.total;
    },
    
    /**
     * Update cart badge in UI
     */
    updateCartBadge() {
        const count = this.getItemCount();
        const badges = document.querySelectorAll('.cart-badge');
        
        console.log('CartManager.updateCartBadge - count:', count);
        
        badges.forEach(badge => {
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'flex';
                badge.style.visibility = 'visible';
                badge.style.opacity = '1';
            } else {
                badge.textContent = '0';
                badge.style.display = 'none';
                badge.style.visibility = 'hidden';
                badge.style.opacity = '0';
            }
        });
    },
    
    /**
     * Format price to VND
     */
    formatPrice(price) {
        return new Intl.NumberFormat('vi-VN').format(price) + '₫';
    },
    
    /**
     * Sync cart to server (when user logs in)
     * YAME BEHAVIOR: Cart is NOT synced to server
     * Cart only exists in localStorage (per browser)
     */
    async syncToServer() {
        // Yame behavior: Do NOT sync cart to server
        // Cart is browser-specific (localStorage only)
        console.log('Cart sync disabled - Yame behavior: localStorage only');
        return { success: true, message: 'Cart is localStorage-only (Yame behavior)' };
    },
    
    /**
     * Load cart from server
     * YAME BEHAVIOR: Cart is NOT loaded from server
     * Each browser has its own cart (localStorage)
     */
    async loadFromServer() {
        // Yame behavior: Do NOT load cart from server
        // Each browser has its own independent cart
        console.log('Cart load from server disabled - Yame behavior: localStorage only');
        return { success: true, message: 'Cart is localStorage-only (Yame behavior)' };
    }
};

// Initialize cart badge on page load
document.addEventListener('DOMContentLoaded', () => {
    CartManager.updateCartBadge();
});

// Update cart badge when page is shown (including back/forward navigation)
window.addEventListener('pageshow', (event) => {
    console.log('pageshow event triggered, persisted:', event.persisted);
    
    // Always update cart badge when page is shown
    CartManager.updateCartBadge();
    
    // Update cart dropdown if it exists
    if (window.updateCartDropdown) {
        updateCartDropdown();
    }
    
    // Trigger cart update event for other components
    window.dispatchEvent(new CustomEvent('cartUpdated'));
});

// Also listen for visibilitychange (when tab becomes visible)
document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
        console.log('Page became visible - updating cart');
        CartManager.updateCartBadge();
        
        if (window.updateCartDropdown) {
            updateCartDropdown();
        }
    }
});

// Listen for focus event (when window gets focus)
window.addEventListener('focus', () => {
    console.log('Window focused - updating cart');
    CartManager.updateCartBadge();
    
    if (window.updateCartDropdown) {
        updateCartDropdown();
    }
});

// Listen for storage changes (from other tabs or when cart is modified)
window.addEventListener('storage', (e) => {
    if (e.key === CartManager.CART_KEY) {
        console.log('Cart changed in storage - updating display');
        CartManager.updateCartBadge();
        
        if (window.updateCartDropdown) {
            updateCartDropdown();
        }
    }
});

// Listen for custom cart events (same window)
window.addEventListener('cartItemRemoved', () => {
    console.log('Cart item removed - updating display');
    CartManager.updateCartBadge();
    
    if (window.updateCartDropdown) {
        updateCartDropdown();
    }
});

window.addEventListener('cartUpdated', () => {
    console.log('Cart updated - refreshing display');
    CartManager.updateCartBadge();
    
    if (window.updateCartDropdown) {
        updateCartDropdown();
    }
});

// Force update on every page load (even from cache)
// This ensures cart is always in sync
(function forceCartUpdate() {
    // Run immediately
    CartManager.updateCartBadge();
    
    // Run again after a short delay to catch any late-loading elements
    setTimeout(() => {
        CartManager.updateCartBadge();
        if (window.updateCartDropdown) {
            updateCartDropdown();
        }
    }, 100);
    
    // Run one more time after DOM is fully ready
    setTimeout(() => {
        CartManager.updateCartBadge();
        if (window.updateCartDropdown) {
            updateCartDropdown();
        }
    }, 500);
})();

// Export for use in other scripts
window.CartManager = CartManager;
