// Enhanced E-commerce JavaScript - Đặc Sản Quê Hương

document.addEventListener('DOMContentLoaded', function() {
    console.log('🛒 Đặc Sản Quê Hương - Enhanced E-commerce Loaded');
    
    // Initialize all features
    initializeEnhancedFeatures();
    initializeProductInteractions();
    initializeCartEnhancements();
    initializeSearchEnhancements();
    initializeAnimations();
    initializeNotifications();
    initializePerformanceOptimizations();
});

// Enhanced Features Initialization
function initializeEnhancedFeatures() {
    // Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Enhanced navbar scroll effects
    initializeNavbarEffects();
    
    // Initialize tooltips
    initializeTooltips();
    
    // Initialize lazy loading
    initializeLazyLoading();
    
    // Initialize keyboard shortcuts
    initializeKeyboardShortcuts();
}

// Enhanced Navbar Effects
function initializeNavbarEffects() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;
    
    let lastScrollTop = 0;
    let ticking = false;
    
    function updateNavbar() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        // Add/remove scrolled class
        if (scrollTop > 50) {
            navbar.classList.add('navbar-scrolled');
        } else {
            navbar.classList.remove('navbar-scrolled');
        }
        
        // Hide/show navbar on scroll
        if (scrollTop > lastScrollTop && scrollTop > 100) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }
        
        lastScrollTop = scrollTop;
        ticking = false;
    }
    
    window.addEventListener('scroll', function() {
        if (!ticking) {
            requestAnimationFrame(updateNavbar);
            ticking = true;
        }
    });
}

// Product Interactions
function initializeProductInteractions() {
    // Enhanced add to cart functionality
    document.querySelectorAll('.add-to-cart-btn, [data-product-id]').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const productId = this.getAttribute('data-product-id');
            const quantity = this.getAttribute('data-quantity') || 1;
            
            if (productId) {
                addToCartEnhanced(productId, quantity, this);
            }
        });
    });
    
    // Product quick view
    document.querySelectorAll('.product-card').forEach(card => {
        const quickViewBtn = document.createElement('button');
        quickViewBtn.className = 'btn btn-outline-primary btn-sm position-absolute';
        quickViewBtn.style.cssText = 'top: 10px; left: 10px; opacity: 0; transition: opacity 0.3s ease;';
        quickViewBtn.innerHTML = '<i class="bi bi-eye"></i>';
        quickViewBtn.title = 'Xem nhanh';
        
        card.style.position = 'relative';
        card.appendChild(quickViewBtn);
        
        card.addEventListener('mouseenter', () => {
            quickViewBtn.style.opacity = '1';
        });
        
        card.addEventListener('mouseleave', () => {
            quickViewBtn.style.opacity = '0';
        });
        
        quickViewBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            // Implement quick view modal
            showQuickViewModal(card);
        });
    });
    
    // Product image zoom effect
    document.querySelectorAll('.product-img').forEach(img => {
        img.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.1)';
        });
        
        img.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });
}

// Enhanced Add to Cart
function addToCartEnhanced(productId, quantity, buttonElement) {
    // Show loading state
    const originalContent = buttonElement.innerHTML;
    buttonElement.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Đang thêm...';
    buttonElement.disabled = true;
    
    // Add CSS for spin animation if not exists
    if (!document.querySelector('#spin-animation')) {
        const style = document.createElement('style');
        style.id = 'spin-animation';
        style.textContent = `
            .spin {
                animation: spin 1s linear infinite;
            }
            @keyframes spin {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }
    
    const token = localStorage.getItem('accessToken');
    
    if (!token) {
        showEnhancedNotification('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'warning');
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
        return;
    }
    
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            productId: parseInt(productId),
            quantity: parseInt(quantity)
        })
    })
    .then(response => {
        if (response.status === 401) {
            localStorage.clear();
            showEnhancedNotification('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'warning');
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
            return;
        }
        return response.json();
    })
    .then(data => {
        if (data && data.success) {
            // Update cart badge with animation
            updateCartBadgeEnhanced(data.cartItemCount);
            
            // Show success notification
            showEnhancedNotification('Đã thêm sản phẩm vào giỏ hàng!', 'success');
            
            // Success animation for button
            buttonElement.innerHTML = '<i class="bi bi-check-circle text-success"></i> Đã thêm!';
            buttonElement.classList.add('btn-success');
            
            // Reset button after 2 seconds
            setTimeout(() => {
                buttonElement.innerHTML = originalContent;
                buttonElement.classList.remove('btn-success');
                buttonElement.disabled = false;
            }, 2000);
            
            // Add floating cart animation
            createFloatingCartAnimation(buttonElement);
            
        } else if (data) {
            showEnhancedNotification(data.message || 'Có lỗi xảy ra khi thêm sản phẩm', 'error');
            buttonElement.innerHTML = originalContent;
            buttonElement.disabled = false;
        }
    })
    .catch(error => {
        console.error('Error adding to cart:', error);
        showEnhancedNotification('Có lỗi xảy ra. Vui lòng thử lại.', 'error');
        buttonElement.innerHTML = originalContent;
        buttonElement.disabled = false;
    });
}

// Enhanced Cart Badge Update
function updateCartBadgeEnhanced(count) {
    const badge = document.querySelector('.navbar .badge, .cart-badge');
    if (badge) {
        // Animate the badge
        badge.style.transform = 'scale(1.5)';
        badge.style.transition = 'transform 0.3s ease';
        
        setTimeout(() => {
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'inline';
            } else {
                badge.style.display = 'none';
            }
            
            badge.style.transform = 'scale(1)';
        }, 150);
        
        // Add bounce animation
        badge.classList.add('animate-bounce');
        setTimeout(() => {
            badge.classList.remove('animate-bounce');
        }, 1000);
    }
}

// Floating Cart Animation
function createFloatingCartAnimation(sourceElement) {
    const cartIcon = document.querySelector('.navbar a[href*="/cart"] i');
    if (!cartIcon || !sourceElement) return;
    
    const sourceRect = sourceElement.getBoundingClientRect();
    const cartRect = cartIcon.getBoundingClientRect();
    
    // Create floating element
    const floatingElement = document.createElement('div');
    floatingElement.innerHTML = '<i class="bi bi-cart-plus"></i>';
    floatingElement.style.cssText = `
        position: fixed;
        left: ${sourceRect.left + sourceRect.width / 2}px;
        top: ${sourceRect.top + sourceRect.height / 2}px;
        z-index: 9999;
        color: var(--primary-color);
        font-size: 1.5rem;
        pointer-events: none;
        transition: all 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    `;
    
    document.body.appendChild(floatingElement);
    
    // Animate to cart
    setTimeout(() => {
        floatingElement.style.left = cartRect.left + 'px';
        floatingElement.style.top = cartRect.top + 'px';
        floatingElement.style.transform = 'scale(0.5)';
        floatingElement.style.opacity = '0';
    }, 100);
    
    // Remove element after animation
    setTimeout(() => {
        floatingElement.remove();
    }, 900);
}

// Cart Enhancements
function initializeCartEnhancements() {
    // Auto-save cart changes
    document.querySelectorAll('.quantity-input').forEach(input => {
        let timeout;
        input.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                const cartItemId = this.getAttribute('data-cart-item-id');
                const quantity = parseInt(this.value);
                
                if (cartItemId && quantity > 0) {
                    updateCartItemQuantity(cartItemId, quantity);
                }
            }, 1000); // Auto-save after 1 second of no input
        });
    });
    
    // Cart item removal with undo functionality
    document.querySelectorAll('.remove-cart-item').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const cartItemId = this.getAttribute('data-cart-item-id');
            removeCartItemWithUndo(cartItemId, this.closest('.cart-item'));
        });
    });
}

// Remove Cart Item with Undo
function removeCartItemWithUndo(cartItemId, itemElement) {
    // Store original content for undo
    const originalContent = itemElement.innerHTML;
    
    // Show undo interface
    itemElement.innerHTML = `
        <div class="alert alert-warning d-flex justify-content-between align-items-center">
            <span><i class="bi bi-trash me-2"></i>Sản phẩm đã được xóa</span>
            <div>
                <button class="btn btn-sm btn-outline-primary me-2" onclick="undoRemoveItem('${cartItemId}', this)">
                    <i class="bi bi-arrow-counterclockwise me-1"></i>Hoàn tác
                </button>
                <button class="btn btn-sm btn-danger" onclick="confirmRemoveItem('${cartItemId}', this)">
                    <i class="bi bi-check me-1"></i>Xác nhận
                </button>
            </div>
        </div>
    `;
    
    // Store original content for undo
    itemElement.setAttribute('data-original-content', originalContent);
    
    // Auto-confirm after 10 seconds
    setTimeout(() => {
        if (itemElement.querySelector('.alert-warning')) {
            confirmRemoveItem(cartItemId, itemElement.querySelector('.btn-danger'));
        }
    }, 10000);
}

// Undo Remove Item
window.undoRemoveItem = function(cartItemId, buttonElement) {
    const itemElement = buttonElement.closest('.cart-item');
    const originalContent = itemElement.getAttribute('data-original-content');
    
    if (originalContent) {
        itemElement.innerHTML = originalContent;
        showEnhancedNotification('Đã hoàn tác xóa sản phẩm', 'info');
    }
};

// Confirm Remove Item
window.confirmRemoveItem = function(cartItemId, buttonElement) {
    const itemElement = buttonElement.closest('.cart-item');
    
    fetch('/api/cart/remove', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        },
        body: JSON.stringify({ cartItemId: cartItemId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Animate removal
            itemElement.style.transition = 'all 0.5s ease';
            itemElement.style.transform = 'translateX(-100%)';
            itemElement.style.opacity = '0';
            
            setTimeout(() => {
                itemElement.remove();
                updateCartTotals();
            }, 500);
            
            showEnhancedNotification('Đã xóa sản phẩm khỏi giỏ hàng', 'success');
        } else {
            showEnhancedNotification('Có lỗi xảy ra khi xóa sản phẩm', 'error');
        }
    })
    .catch(error => {
        console.error('Error removing item:', error);
        showEnhancedNotification('Có lỗi xảy ra. Vui lòng thử lại.', 'error');
    });
};

// Search Enhancements
function initializeSearchEnhancements() {
    const searchInput = document.querySelector('input[name="keyword"]');
    if (!searchInput) return;
    
    let searchTimeout;
    let searchSuggestions = null;
    
    // Create suggestions dropdown
    const suggestionsContainer = document.createElement('div');
    suggestionsContainer.className = 'search-suggestions';
    suggestionsContainer.style.cssText = `
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border: 1px solid #dee2e6;
        border-top: none;
        border-radius: 0 0 10px 10px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        max-height: 300px;
        overflow-y: auto;
        z-index: 1000;
        display: none;
    `;
    
    searchInput.parentElement.style.position = 'relative';
    searchInput.parentElement.appendChild(suggestionsContainer);
    
    // Enhanced search with suggestions
    searchInput.addEventListener('input', function() {
        const query = this.value.trim();
        
        clearTimeout(searchTimeout);
        
        if (query.length >= 2) {
            searchTimeout = setTimeout(() => {
                fetchSearchSuggestions(query, suggestionsContainer);
            }, 300);
        } else {
            hideSuggestions(suggestionsContainer);
        }
    });
    
    // Hide suggestions when clicking outside
    document.addEventListener('click', function(e) {
        if (!searchInput.parentElement.contains(e.target)) {
            hideSuggestions(suggestionsContainer);
        }
    });
    
    // Keyboard navigation for suggestions
    searchInput.addEventListener('keydown', function(e) {
        const suggestions = suggestionsContainer.querySelectorAll('.suggestion-item');
        const activeSuggestion = suggestionsContainer.querySelector('.suggestion-item.active');
        
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (activeSuggestion) {
                activeSuggestion.classList.remove('active');
                const next = activeSuggestion.nextElementSibling;
                if (next) {
                    next.classList.add('active');
                } else {
                    suggestions[0]?.classList.add('active');
                }
            } else {
                suggestions[0]?.classList.add('active');
            }
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            if (activeSuggestion) {
                activeSuggestion.classList.remove('active');
                const prev = activeSuggestion.previousElementSibling;
                if (prev) {
                    prev.classList.add('active');
                } else {
                    suggestions[suggestions.length - 1]?.classList.add('active');
                }
            } else {
                suggestions[suggestions.length - 1]?.classList.add('active');
            }
        } else if (e.key === 'Enter') {
            if (activeSuggestion) {
                e.preventDefault();
                activeSuggestion.click();
            }
        } else if (e.key === 'Escape') {
            hideSuggestions(suggestionsContainer);
        }
    });
}

// Fetch Search Suggestions
function fetchSearchSuggestions(query, container) {
    fetch(`/api/search/suggestions?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            displaySearchSuggestions(data, container, query);
        })
        .catch(error => {
            console.error('Error fetching suggestions:', error);
        });
}

// Display Search Suggestions
function displaySearchSuggestions(suggestions, container, query) {
    if (!suggestions || suggestions.length === 0) {
        hideSuggestions(container);
        return;
    }
    
    container.innerHTML = '';
    
    suggestions.forEach(suggestion => {
        const item = document.createElement('div');
        item.className = 'suggestion-item';
        item.style.cssText = `
            padding: 0.75rem 1rem;
            cursor: pointer;
            border-bottom: 1px solid #f8f9fa;
            transition: background-color 0.2s ease;
        `;
        
        item.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="bi bi-search me-2 text-muted"></i>
                <span>${highlightQuery(suggestion.name, query)}</span>
            </div>
        `;
        
        item.addEventListener('mouseenter', function() {
            container.querySelectorAll('.suggestion-item').forEach(s => s.classList.remove('active'));
            this.classList.add('active');
        });
        
        item.addEventListener('click', function() {
            window.location.href = `/products/search?keyword=${encodeURIComponent(suggestion.name)}`;
        });
        
        container.appendChild(item);
    });
    
    container.style.display = 'block';
}

// Highlight Query in Suggestions
function highlightQuery(text, query) {
    const regex = new RegExp(`(${query})`, 'gi');
    return text.replace(regex, '<strong>$1</strong>');
}

// Hide Suggestions
function hideSuggestions(container) {
    container.style.display = 'none';
}

// Enhanced Animations
function initializeAnimations() {
    // Intersection Observer for scroll animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-fade-in-up');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe elements for animation
    document.querySelectorAll('.card, .product-card, .feature-item, .category-card').forEach(el => {
        observer.observe(el);
    });
    
    // Parallax effect for hero sections
    const heroSections = document.querySelectorAll('.hero-section, .page-header');
    
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        
        heroSections.forEach(section => {
            const rate = scrolled * -0.5;
            section.style.transform = `translateY(${rate}px)`;
        });
    });
    
    // Stagger animations for product grids
    document.querySelectorAll('.product-card').forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
    });
}

// Enhanced Notifications
function initializeNotifications() {
    // Create notification container if it doesn't exist
    if (!document.getElementById('notification-container')) {
        const container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            max-width: 400px;
        `;
        document.body.appendChild(container);
    }
}

// Show Enhanced Notification
function showEnhancedNotification(message, type = 'info', duration = 5000) {
    const container = document.getElementById('notification-container');
    if (!container) return;
    
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show notification-enhanced`;
    notification.style.cssText = `
        margin-bottom: 1rem;
        border: none;
        border-radius: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideInRight 0.3s ease-out;
    `;
    
    const icons = {
        success: 'bi-check-circle',
        error: 'bi-exclamation-triangle',
        warning: 'bi-exclamation-triangle',
        info: 'bi-info-circle'
    };
    
    notification.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="bi ${icons[type] || icons.info} me-2"></i>
            <span>${message}</span>
            <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    container.appendChild(notification);
    
    // Auto remove after duration
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease-out';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }, duration);
    
    // Add CSS animations if not exists
    if (!document.querySelector('#notification-animations')) {
        const style = document.createElement('style');
        style.id = 'notification-animations';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes slideOutRight {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
}

// Tooltips
function initializeTooltips() {
    // Simple tooltip implementation
    document.querySelectorAll('[title]').forEach(element => {
        const title = element.getAttribute('title');
        if (!title) return;
        
        element.removeAttribute('title');
        element.setAttribute('data-tooltip', title);
        
        element.addEventListener('mouseenter', function(e) {
            showTooltip(e.target, title);
        });
        
        element.addEventListener('mouseleave', function() {
            hideTooltip();
        });
    });
}

// Show Tooltip
function showTooltip(element, text) {
    const tooltip = document.createElement('div');
    tooltip.id = 'custom-tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: #333;
        color: white;
        padding: 0.5rem 0.75rem;
        border-radius: 4px;
        font-size: 0.875rem;
        z-index: 10001;
        pointer-events: none;
        opacity: 0;
        transition: opacity 0.2s ease;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';
    
    setTimeout(() => {
        tooltip.style.opacity = '1';
    }, 10);
}

// Hide Tooltip
function hideTooltip() {
    const tooltip = document.getElementById('custom-tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

// Lazy Loading
function initializeLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        images.forEach(img => imageObserver.observe(img));
    } else {
        // Fallback for browsers without IntersectionObserver
        images.forEach(img => {
            img.src = img.dataset.src;
        });
    }
}

// Keyboard Shortcuts
function initializeKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K for search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.querySelector('input[name="keyword"]');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // Escape to close modals and dropdowns
        if (e.key === 'Escape') {
            // Close modals
            document.querySelectorAll('.modal.show').forEach(modal => {
                const modalInstance = bootstrap.Modal.getInstance(modal);
                if (modalInstance) {
                    modalInstance.hide();
                }
            });
            
            // Close dropdowns
            document.querySelectorAll('.dropdown-menu.show').forEach(dropdown => {
                dropdown.classList.remove('show');
            });
            
            // Hide search suggestions
            const suggestions = document.querySelector('.search-suggestions');
            if (suggestions) {
                hideSuggestions(suggestions);
            }
        }
    });
}

// Performance Optimizations
function initializePerformanceOptimizations() {
    // Debounce scroll events
    let scrollTimeout;
    const originalScrollHandler = window.onscroll;
    
    window.onscroll = function() {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(() => {
            if (originalScrollHandler) {
                originalScrollHandler();
            }
        }, 16); // ~60fps
    };
    
    // Preload critical resources
    preloadCriticalResources();
    
    // Optimize images
    optimizeImages();
}

// Preload Critical Resources
function preloadCriticalResources() {
    const criticalResources = [
        '/css/modern-ecommerce.css',
        '/js/app.js'
    ];
    
    criticalResources.forEach(resource => {
        const link = document.createElement('link');
        link.rel = 'preload';
        link.href = resource;
        link.as = resource.endsWith('.css') ? 'style' : 'script';
        document.head.appendChild(link);
    });
}

// Optimize Images
function optimizeImages() {
    document.querySelectorAll('img').forEach(img => {
        // Add loading="lazy" for better performance
        if (!img.hasAttribute('loading')) {
            img.setAttribute('loading', 'lazy');
        }
        
        // Add error handling
        img.addEventListener('error', function() {
            this.src = 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=300&q=80';
        });
    });
}

// Quick View Modal
function showQuickViewModal(productCard) {
    // Extract product information
    const productName = productCard.querySelector('.product-title, .card-title')?.textContent || 'Sản phẩm';
    const productPrice = productCard.querySelector('.product-price, .price-tag')?.textContent || '0₫';
    const productImage = productCard.querySelector('img')?.src || '';
    const productDescription = productCard.querySelector('.product-description, .card-text')?.textContent || '';
    
    // Create modal HTML
    const modalHTML = `
        <div class="modal fade" id="quickViewModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Xem nhanh sản phẩm</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <img src="${productImage}" class="img-fluid rounded" alt="${productName}">
                            </div>
                            <div class="col-md-6">
                                <h4 class="fw-bold mb-3">${productName}</h4>
                                <p class="text-muted mb-3">${productDescription}</p>
                                <div class="mb-4">
                                    <span class="h4 text-primary fw-bold">${productPrice}</span>
                                </div>
                                <div class="d-grid gap-2">
                                    <button class="btn btn-primary btn-lg">
                                        <i class="bi bi-cart-plus me-2"></i>Thêm vào giỏ hàng
                                    </button>
                                    <button class="btn btn-outline-secondary">
                                        <i class="bi bi-eye me-2"></i>Xem chi tiết
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal
    const existingModal = document.getElementById('quickViewModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Add modal to DOM
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('quickViewModal'));
    modal.show();
    
    // Clean up when modal is hidden
    document.getElementById('quickViewModal').addEventListener('hidden.bs.modal', function() {
        this.remove();
    });
}

// Update Cart Totals
function updateCartTotals() {
    // This would typically recalculate and update cart totals
    // Implementation depends on your cart structure
    console.log('Updating cart totals...');
}

// Export functions for global use
window.showEnhancedNotification = showEnhancedNotification;
window.addToCartEnhanced = addToCartEnhanced;
window.updateCartBadgeEnhanced = updateCartBadgeEnhanced;