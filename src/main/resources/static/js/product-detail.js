/**
 * Product Detail Page JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeProductDetail();
});

function initializeProductDetail() {
    initializeImageGallery();
    initializeQuantityControls();
    initializeAddToCart();
    initializeTabs();
    initializeWishlist();
}

// Image Gallery
function initializeImageGallery() {
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('mainProductImage');
    
    thumbnails.forEach(thumb => {
        thumb.addEventListener('click', function() {
            // Remove active class from all
            thumbnails.forEach(t => t.classList.remove('active'));
            // Add active to clicked
            this.classList.add('active');
            // Change main image
            mainImage.src = this.src;
            mainImage.style.transform = 'scale(0.95)';
            setTimeout(() => {
                mainImage.style.transform = 'scale(1)';
            }, 100);
        });
    });
    
    // Set first thumbnail as active
    if (thumbnails.length > 0) {
        thumbnails[0].classList.add('active');
    }
}

// Quantity Controls
function initializeQuantityControls() {
    const decreaseBtn = document.getElementById('decreaseQty');
    const increaseBtn = document.getElementById('increaseQty');
    const quantityInput = document.getElementById('quantity');
    
    if (!quantityInput) return;
    
    const min = parseInt(quantityInput.getAttribute('min')) || 1;
    const max = parseInt(quantityInput.getAttribute('max')) || 999;
    
    if (decreaseBtn) {
        decreaseBtn.addEventListener('click', function() {
            let value = parseInt(quantityInput.value);
            if (value > min) {
                quantityInput.value = value - 1;
            }
        });
    }
    
    if (increaseBtn) {
        increaseBtn.addEventListener('click', function() {
            let value = parseInt(quantityInput.value);
            if (value < max) {
                quantityInput.value = value + 1;
            }
        });
    }
    
    // Validate input
    quantityInput.addEventListener('change', function() {
        let value = parseInt(this.value);
        if (isNaN(value) || value < min) {
            this.value = min;
        } else if (value > max) {
            this.value = max;
        }
    });
}


// Add to Cart
function initializeAddToCart() {
    const addToCartBtn = document.querySelector('.btn-add-cart');
    const buyNowBtn = document.querySelector('.btn-buy-now');
    
    if (addToCartBtn) {
        // Mark this button as handled by product-detail.js to prevent double handling
        addToCartBtn.setAttribute('data-detail-page-handler', 'true');
        
        addToCartBtn.addEventListener('click', function(e) {
            // Prevent event from bubbling to ecommerce-enhanced.js handler
            e.stopPropagation();
            
            // Prevent double execution
            if (this.disabled) return;
            
            const productId = this.getAttribute('data-product-id');
            const quantityInput = document.getElementById('quantity');
            const quantity = quantityInput ? quantityInput.value : 1;
            const productName = document.querySelector('.product-title').textContent;
            const priceText = document.querySelector('.current-price').textContent;
            const price = parseFloat(priceText.replace(/[^\d]/g, ''));
            const imageUrl = document.getElementById('mainProductImage').src;
            
            // Show loading
            const originalContent = this.innerHTML;
            this.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Đang thêm...';
            this.disabled = true;
            
            // Add to cart using CartManager
            if (window.CartManager) {
                CartManager.addItem(parseInt(productId), productName, price, parseInt(quantity), imageUrl);
                
                // Success
                this.innerHTML = '<i class="bi bi-check-circle"></i> Đã thêm!';
                this.classList.add('btn-success');
                
                // Animations
                if (window.scrollToShowHeader) scrollToShowHeader();
                if (window.createProductBubbleAnimation) createProductBubbleAnimation(this, imageUrl);
                
                setTimeout(() => {
                    if (window.showCartDropdown) showCartDropdown();
                }, 800);
                
                // Reset button
                setTimeout(() => {
                    this.innerHTML = originalContent;
                    this.classList.remove('btn-success');
                    this.disabled = false;
                }, 2000);
            }
        });
    }
    
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Get product info
            const productId = addToCartBtn.getAttribute('data-product-id');
            const quantityInput = document.getElementById('quantity');
            const quantity = quantityInput ? quantityInput.value : 1;
            const productName = document.querySelector('.product-title').textContent;
            const priceText = document.querySelector('.current-price').textContent;
            const price = parseFloat(priceText.replace(/[^\d]/g, ''));
            const imageUrl = document.getElementById('mainProductImage').src;
            
            // Add to cart using CartManager (localStorage)
            if (window.CartManager) {
                CartManager.addItem(parseInt(productId), productName, price, parseInt(quantity), imageUrl);
                
                // Show notification
                if (window.showEnhancedNotification) {
                    showEnhancedNotification('Đang chuyển đến trang thanh toán...', 'success');
                }
                
                // Redirect to checkout immediately (works for both guest and logged-in users)
                setTimeout(() => {
                    window.location.href = '/checkout';
                }, 500);
            } else {
                // Fallback if CartManager not available
                window.location.href = '/checkout';
            }
        });
    }
}

// Tabs
function initializeTabs() {
    const tabButtons = document.querySelectorAll('[data-bs-toggle="tab"]');
    tabButtons.forEach(button => {
        button.addEventListener('shown.bs.tab', function(e) {
            console.log('Tab switched:', e.target.getAttribute('data-bs-target'));
        });
    });
}

// Wishlist
function initializeWishlist() {
    const wishlistBtn = document.querySelector('.btn-wishlist');
    if (wishlistBtn) {
        wishlistBtn.addEventListener('click', function() {
            this.classList.toggle('active');
            if (this.classList.contains('active')) {
                this.innerHTML = '<i class="bi bi-heart-fill"></i>';
                this.style.color = '#dc3545';
                this.style.borderColor = '#dc3545';
                showNotification('Đã thêm vào danh sách yêu thích', 'success');
            } else {
                this.innerHTML = '<i class="bi bi-heart"></i>';
                this.style.color = '';
                this.style.borderColor = '';
                showNotification('Đã xóa khỏi danh sách yêu thích', 'info');
            }
        });
    }
}

// Notification
function showNotification(message, type = 'info') {
    if (window.showEnhancedNotification) {
        showEnhancedNotification(message, type);
    } else {
        alert(message);
    }
}

// Add spin animation CSS
const style = document.createElement('style');
style.textContent = `
    .spin {
        animation: spin 1s linear infinite;
    }
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
    .btn-success {
        background: linear-gradient(135deg, #28a745, #20c997) !important;
    }
`;
document.head.appendChild(style);
