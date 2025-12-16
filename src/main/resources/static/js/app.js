// Main JavaScript file for Specialty Food E-commerce Website

document.addEventListener('DOMContentLoaded', function() {
    console.log('Specialty Food E-commerce Application Loaded');
    
    // Initialize application
    initializeApp();
    
    // Initialize animations
    initializeAnimations();
    
    // Initialize scroll effects
    initializeScrollEffects();
    
    // Initialize interactive effects
    initializeInteractiveEffects();
    
    // Initialize particle effects
    initializeParticleEffects();
});

function initializeApp() {
    // Initialize accessibility features
    initializeAccessibility();
    
    // Initialize cart functionality
    initializeCart();
    
    // Initialize search functionality
    initializeSearch();
    
    // Initialize responsive features
    initializeResponsive();
    
    // Initialize loading indicators
    initializeLoadingIndicators();
}

// Accessibility features
function initializeAccessibility() {
    // Skip to main content functionality
    initializeSkipLinks();
    
    // Keyboard navigation
    initializeKeyboardNavigation();
    
    // Focus management
    initializeFocusManagement();
    
    // ARIA live regions
    initializeAriaLiveRegions();
    
    // Screen reader announcements
    initializeScreenReaderAnnouncements();
}

// Skip to main content
function initializeSkipLinks() {
    const skipLink = document.querySelector('.visually-hidden-focusable[href="#main-content"]');
    const mainContent = document.getElementById('main-content');
    
    if (skipLink && mainContent) {
        skipLink.addEventListener('click', function(e) {
            e.preventDefault();
            mainContent.focus();
            mainContent.scrollIntoView();
        });
    }
}

// Keyboard navigation
function initializeKeyboardNavigation() {
    // Escape key to close modals and dropdowns
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            // Close open modals
            const openModals = document.querySelectorAll('.modal.show');
            openModals.forEach(modal => {
                const modalInstance = bootstrap.Modal.getInstance(modal);
                if (modalInstance) {
                    modalInstance.hide();
                }
            });
            
            // Close open dropdowns
            const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
            openDropdowns.forEach(dropdown => {
                const dropdownToggle = dropdown.previousElementSibling;
                if (dropdownToggle) {
                    const dropdownInstance = bootstrap.Dropdown.getInstance(dropdownToggle);
                    if (dropdownInstance) {
                        dropdownInstance.hide();
                    }
                }
            });
        }
    });
    
    // Arrow key navigation for dropdowns
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
        const items = menu.querySelectorAll('.dropdown-item');
        
        menu.addEventListener('keydown', function(e) {
            let currentIndex = Array.from(items).indexOf(document.activeElement);
            
            switch(e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    currentIndex = (currentIndex + 1) % items.length;
                    items[currentIndex].focus();
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    currentIndex = currentIndex <= 0 ? items.length - 1 : currentIndex - 1;
                    items[currentIndex].focus();
                    break;
                case 'Home':
                    e.preventDefault();
                    items[0].focus();
                    break;
                case 'End':
                    e.preventDefault();
                    items[items.length - 1].focus();
                    break;
            }
        });
    });
    
    // Tab navigation for product cards
    document.querySelectorAll('[role="article"]').forEach(card => {
        card.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                const link = card.querySelector('a');
                if (link) {
                    e.preventDefault();
                    link.click();
                }
            }
        });
    });
}

// Focus management
function initializeFocusManagement() {
    // Trap focus in modals
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('shown.bs.modal', function() {
            const focusableElements = modal.querySelectorAll(
                'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
            );
            
            if (focusableElements.length > 0) {
                focusableElements[0].focus();
            }
        });
        
        modal.addEventListener('keydown', function(e) {
            if (e.key === 'Tab') {
                const focusableElements = modal.querySelectorAll(
                    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
                );
                
                const firstElement = focusableElements[0];
                const lastElement = focusableElements[focusableElements.length - 1];
                
                if (e.shiftKey && document.activeElement === firstElement) {
                    e.preventDefault();
                    lastElement.focus();
                } else if (!e.shiftKey && document.activeElement === lastElement) {
                    e.preventDefault();
                    firstElement.focus();
                }
            }
        });
    });
    
    // Return focus after modal closes
    let lastFocusedElement = null;
    
    document.querySelectorAll('[data-bs-toggle="modal"]').forEach(trigger => {
        trigger.addEventListener('click', function() {
            lastFocusedElement = this;
        });
    });
    
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('hidden.bs.modal', function() {
            if (lastFocusedElement) {
                lastFocusedElement.focus();
                lastFocusedElement = null;
            }
        });
    });
}

// ARIA live regions
function initializeAriaLiveRegions() {
    // Create live region for announcements if it doesn't exist
    if (!document.getElementById('aria-live-region')) {
        const liveRegion = document.createElement('div');
        liveRegion.id = 'aria-live-region';
        liveRegion.setAttribute('aria-live', 'polite');
        liveRegion.setAttribute('aria-atomic', 'true');
        liveRegion.className = 'visually-hidden';
        document.body.appendChild(liveRegion);
    }
}

// Screen reader announcements
function initializeScreenReaderAnnouncements() {
    // Announce page changes
    const pageTitle = document.title;
    announceToScreenReader(`Đã tải trang: ${pageTitle}`);
}

// Announce message to screen readers
function announceToScreenReader(message) {
    const liveRegion = document.getElementById('aria-live-region');
    if (liveRegion) {
        liveRegion.textContent = message;
        
        // Clear after announcement
        setTimeout(() => {
            liveRegion.textContent = '';
        }, 1000);
    }
}

// Cart functionality
function initializeCart() {
    // Add to cart buttons
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');
            const quantity = this.getAttribute('data-quantity') || 1;
            
            addToCart(productId, quantity);
        });
    });
    
    // Update cart item quantity
    document.querySelectorAll('.cart-quantity-input').forEach(input => {
        input.addEventListener('change', function() {
            const cartItemId = this.getAttribute('data-cart-item-id');
            const quantity = parseInt(this.value);
            
            if (quantity > 0) {
                updateCartItem(cartItemId, quantity);
            }
        });
    });
    
    // Remove cart item buttons
    document.querySelectorAll('.remove-cart-item-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const cartItemId = this.getAttribute('data-cart-item-id');
            
            removeCartItem(cartItemId);
        });
    });
}

// Add product to cart
function addToCart(productId, quantity = 1) {
    showLoadingSpinner();
    
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            productId: productId,
            quantity: quantity
        })
    })
    .then(response => response.json())
    .then(data => {
        hideLoadingSpinner();
        
        if (data.success) {
            // Update cart badge
            updateCartBadge(data.cartItemCount);
            
            // Show success message
            showAlert('success', 'Đã thêm sản phẩm vào giỏ hàng!');
            
            // Animate cart badge
            animateCartBadge();
        } else {
            showAlert('danger', data.message || 'Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng.');
        }
    })
    .catch(error => {
        hideLoadingSpinner();
        console.error('Error adding to cart:', error);
        showAlert('danger', 'Có lỗi xảy ra. Vui lòng thử lại.');
    });
}

// Update cart item quantity
function updateCartItem(cartItemId, quantity) {
    showLoadingSpinner();
    
    fetch('/api/cart/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            cartItemId: cartItemId,
            quantity: quantity
        })
    })
    .then(response => response.json())
    .then(data => {
        hideLoadingSpinner();
        
        if (data.success) {
            // Reload cart page or update totals
            location.reload();
        } else {
            showAlert('danger', data.message || 'Có lỗi xảy ra khi cập nhật giỏ hàng.');
        }
    })
    .catch(error => {
        hideLoadingSpinner();
        console.error('Error updating cart:', error);
        showAlert('danger', 'Có lỗi xảy ra. Vui lòng thử lại.');
    });
}

// Remove cart item
function removeCartItem(cartItemId) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
        return;
    }
    
    showLoadingSpinner();
    
    fetch('/api/cart/remove', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            cartItemId: cartItemId
        })
    })
    .then(response => response.json())
    .then(data => {
        hideLoadingSpinner();
        
        if (data.success) {
            // Reload cart page
            location.reload();
        } else {
            showAlert('danger', data.message || 'Có lỗi xảy ra khi xóa sản phẩm.');
        }
    })
    .catch(error => {
        hideLoadingSpinner();
        console.error('Error removing cart item:', error);
        showAlert('danger', 'Có lỗi xảy ra. Vui lòng thử lại.');
    });
}

// Search functionality
function initializeSearch() {
    const searchForm = document.querySelector('form[action*="/products/search"]');
    const searchInput = document.querySelector('input[name="keyword"]');
    
    if (searchForm && searchInput) {
        // Auto-submit search after typing (debounced)
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                if (this.value.length >= 2) {
                    // Show search suggestions (if implemented)
                    showSearchSuggestions(this.value);
                }
            }, 300);
        });
    }
}

// Show search suggestions (placeholder for future implementation)
function showSearchSuggestions(query) {
    // This would show a dropdown with search suggestions
    console.log('Search suggestions for:', query);
}

// Responsive features
function initializeResponsive() {
    // Handle mobile menu
    const navbarToggler = document.querySelector('.navbar-toggler');
    const navbarCollapse = document.querySelector('.navbar-collapse');
    
    if (navbarToggler && navbarCollapse) {
        navbarToggler.addEventListener('click', function() {
            navbarCollapse.classList.toggle('show');
        });
        
        // Close mobile menu when clicking outside
        document.addEventListener('click', function(e) {
            if (!navbarToggler.contains(e.target) && !navbarCollapse.contains(e.target)) {
                navbarCollapse.classList.remove('show');
            }
        });
    }
    
    // Handle responsive images
    handleResponsiveImages();
    
    // Handle responsive tables
    handleResponsiveTables();
}

// Handle responsive images
function handleResponsiveImages() {
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

// Handle responsive tables
function handleResponsiveTables() {
    const tables = document.querySelectorAll('table:not(.table-responsive table)');
    tables.forEach(table => {
        const wrapper = document.createElement('div');
        wrapper.className = 'table-responsive';
        table.parentNode.insertBefore(wrapper, table);
        wrapper.appendChild(table);
    });
}

// Loading indicators
function initializeLoadingIndicators() {
    // Show loading spinner for form submissions
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            showLoadingSpinner();
        });
    });
    
    // Show loading spinner for AJAX links
    document.querySelectorAll('a[data-loading="true"]').forEach(link => {
        link.addEventListener('click', function() {
            showLoadingSpinner();
        });
    });
}

// Show loading spinner
function showLoadingSpinner() {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.remove('d-none');
    }
}

// Hide loading spinner
function hideLoadingSpinner() {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.add('d-none');
    }
}

// Update cart badge
function updateCartBadge(count) {
    const badge = document.querySelector('.navbar .badge');
    if (badge) {
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = 'inline';
        } else {
            badge.style.display = 'none';
        }
    }
}

// Animate cart badge
function animateCartBadge() {
    const cartLink = document.querySelector('.navbar a[href*="/cart"]');
    if (cartLink) {
        cartLink.classList.add('cart-badge-animate');
        setTimeout(() => {
            cartLink.classList.remove('cart-badge-animate');
        }, 600);
    }
}

// Show alert message
function showAlert(type, message) {
    // Remove existing alerts
    document.querySelectorAll('.alert.auto-dismiss').forEach(alert => {
        alert.remove();
    });
    
    // Create new alert
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show auto-dismiss`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.setAttribute('aria-live', type === 'danger' ? 'assertive' : 'polite');
    alertDiv.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle' : 
                          type === 'danger' ? 'exclamation-triangle' : 
                          'info-circle'} me-2" aria-hidden="true"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Đóng thông báo ${type === 'success' ? 'thành công' : 
                                              type === 'danger' ? 'lỗi' : 'thông tin'}"></button>
    `;
    
    // Insert alert at the top of main content
    const main = document.querySelector('main');
    if (main) {
        main.insertBefore(alertDiv, main.firstChild);
        
        // Announce to screen readers
        announceToScreenReader(message);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Animation functions
function initializeAnimations() {
    // Initialize scroll animations
    initializeScrollAnimations();
    
    // Initialize hover effects
    initializeHoverEffects();
    
    // Initialize page transitions
    initializePageTransitions();
}

// Scroll animations
function initializeScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const animationObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const element = entry.target;
                element.classList.add('animate-fade-in-up');
                animationObserver.unobserve(element);
            }
        });
    }, observerOptions);
    
    // Observe elements for animation
    document.querySelectorAll('.card, .hero-section, .featured-categories, .featured-products, .why-choose-us').forEach(el => {
        animationObserver.observe(el);
    });
    
    // Stagger animations for cards
    document.querySelectorAll('.card').forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
    });
}

// Hover effects
function initializeHoverEffects() {
    // Add ripple effect to buttons
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
    
    // Parallax effect for hero section
    const hero = document.querySelector('.hero-section');
    if (hero) {
        window.addEventListener('scroll', () => {
            const scrolled = window.pageYOffset;
            const rate = scrolled * -0.5;
            hero.style.transform = `translateY(${rate}px)`;
        });
    }
}

// Page transitions
function initializePageTransitions() {
    // Smooth page transitions
    document.querySelectorAll('a:not([href^="#"]):not([href^="mailto:"]):not([href^="tel:"]):not([target="_blank"])').forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href && !href.startsWith('javascript:') && !this.hasAttribute('download')) {
                e.preventDefault();
                
                // Add fade out effect
                document.body.style.opacity = '0.8';
                document.body.style.transition = 'opacity 0.3s ease';
                
                setTimeout(() => {
                    window.location.href = href;
                }, 300);
            }
        });
    });
}

// Add CSS for ripple effect
const rippleCSS = `
.btn {
    position: relative;
    overflow: hidden;
}

.ripple {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.6);
    transform: scale(0);
    animation: ripple-animation 0.6s linear;
    pointer-events: none;
}

@keyframes ripple-animation {
    to {
        transform: scale(4);
        opacity: 0;
    }
}
`;

// Inject ripple CSS
const style = document.createElement('style');
style.textContent = rippleCSS;
document.head.appendChild(style);

// Scroll effects for navbar and elements
function initializeScrollEffects() {
    const navbar = document.querySelector('.navbar');
    let lastScrollTop = 0;
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        // Add scrolled class to navbar
        if (scrollTop > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
        
        // Hide/show navbar on scroll
        if (scrollTop > lastScrollTop && scrollTop > 100) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }
        
        lastScrollTop = scrollTop;
    });
    
    // Parallax scrolling for background elements
    const parallaxElements = document.querySelectorAll('.hero-section, .newsletter-signup');
    
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        
        parallaxElements.forEach(element => {
            const speed = element.dataset.speed || 0.5;
            const yPos = -(scrolled * speed);
            element.style.backgroundPosition = `center ${yPos}px`;
        });
    });
}

// Interactive effects
function initializeInteractiveEffects() {
    // Mouse follow effect for cards
    document.querySelectorAll('.card').forEach(card => {
        card.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            this.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)';
        });
    });
    
    // Magnetic effect for buttons
    document.querySelectorAll('.btn-primary, .btn-outline-primary').forEach(button => {
        button.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left - rect.width / 2;
            const y = e.clientY - rect.top - rect.height / 2;
            
            this.style.transform = `translate(${x * 0.1}px, ${y * 0.1}px) scale(1.02)`;
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translate(0, 0) scale(1)';
        });
    });
    
    // Floating animation for icons
    document.querySelectorAll('.bi').forEach((icon, index) => {
        icon.style.animation = `float 3s ease-in-out infinite ${index * 0.1}s`;
    });
    
    // Text reveal animation
    initializeTextReveal();
    
    // Image tilt effect
    initializeImageTilt();
}

// Text reveal animation
function initializeTextReveal() {
    const textElements = document.querySelectorAll('h1, h2, h3, .lead');
    
    textElements.forEach(element => {
        const text = element.textContent;
        element.innerHTML = '';
        
        text.split('').forEach((char, index) => {
            const span = document.createElement('span');
            span.textContent = char === ' ' ? '\u00A0' : char;
            span.style.opacity = '0';
            span.style.transform = 'translateY(20px)';
            span.style.transition = `all 0.5s ease ${index * 0.02}s`;
            element.appendChild(span);
        });
        
        // Trigger animation when element is in view
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.querySelectorAll('span').forEach(span => {
                        span.style.opacity = '1';
                        span.style.transform = 'translateY(0)';
                    });
                    observer.unobserve(entry.target);
                }
            });
        });
        
        observer.observe(element);
    });
}

// Image tilt effect
function initializeImageTilt() {
    document.querySelectorAll('.card-img-top, img').forEach(img => {
        img.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 20;
            const rotateY = (centerX - x) / 20;
            
            this.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.05)`;
        });
        
        img.addEventListener('mouseleave', function() {
            this.style.transform = 'rotateX(0) rotateY(0) scale(1)';
        });
    });
}

// Particle effects
function initializeParticleEffects() {
    // Create floating particles
    createFloatingParticles();
    
    // Mouse trail effect
    initializeMouseTrail();
}

// Create floating particles
function createFloatingParticles() {
    const particleContainer = document.createElement('div');
    particleContainer.className = 'particle-container';
    particleContainer.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: -1;
        overflow: hidden;
    `;
    
    document.body.appendChild(particleContainer);
    
    // Create particles
    for (let i = 0; i < 50; i++) {
        createParticle(particleContainer);
    }
}

function createParticle(container) {
    const particle = document.createElement('div');
    particle.className = 'particle';
    
    const size = Math.random() * 4 + 2;
    const x = Math.random() * window.innerWidth;
    const y = Math.random() * window.innerHeight;
    const duration = Math.random() * 20 + 10;
    
    particle.style.cssText = `
        position: absolute;
        width: ${size}px;
        height: ${size}px;
        background: rgba(37, 99, 235, 0.3);
        border-radius: 50%;
        left: ${x}px;
        top: ${y}px;
        animation: particleFloat ${duration}s linear infinite;
    `;
    
    container.appendChild(particle);
    
    // Remove and recreate particle after animation
    setTimeout(() => {
        particle.remove();
        createParticle(container);
    }, duration * 1000);
}

// Mouse trail effect
function initializeMouseTrail() {
    const trail = [];
    const trailLength = 20;
    
    document.addEventListener('mousemove', function(e) {
        trail.push({ x: e.clientX, y: e.clientY, time: Date.now() });
        
        if (trail.length > trailLength) {
            trail.shift();
        }
        
        updateTrail();
    });
    
    function updateTrail() {
        // Remove old trail elements
        document.querySelectorAll('.mouse-trail').forEach(el => el.remove());
        
        trail.forEach((point, index) => {
            const trailElement = document.createElement('div');
            trailElement.className = 'mouse-trail';
            
            const opacity = index / trailLength;
            const size = (index / trailLength) * 10 + 2;
            
            trailElement.style.cssText = `
                position: fixed;
                left: ${point.x - size/2}px;
                top: ${point.y - size/2}px;
                width: ${size}px;
                height: ${size}px;
                background: rgba(37, 99, 235, ${opacity * 0.5});
                border-radius: 50%;
                pointer-events: none;
                z-index: 9999;
                transition: all 0.1s ease;
            `;
            
            document.body.appendChild(trailElement);
            
            // Remove after short time
            setTimeout(() => {
                trailElement.remove();
            }, 100);
        });
    }
}

// Add additional CSS animations
const additionalCSS = `
@keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
}

@keyframes particleFloat {
    0% {
        transform: translateY(100vh) rotate(0deg);
        opacity: 0;
    }
    10% {
        opacity: 1;
    }
    90% {
        opacity: 1;
    }
    100% {
        transform: translateY(-100px) rotate(360deg);
        opacity: 0;
    }
}

.card {
    transform-style: preserve-3d;
    transition: transform 0.1s ease;
}

.btn {
    transition: transform 0.1s ease;
}

img {
    transition: transform 0.3s ease;
}

/* Smooth scrolling */
html {
    scroll-behavior: smooth;
}

/* Custom scrollbar */
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
}

::-webkit-scrollbar-thumb {
    background: rgba(37, 99, 235, 0.5);
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: rgba(37, 99, 235, 0.7);
}

/* Loading animation */
.loading-pulse {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% { opacity: 1; }
    50% { opacity: 0.5; }
    100% { opacity: 1; }
}

/* Glow effect */
.glow {
    box-shadow: 0 0 20px rgba(37, 99, 235, 0.5);
    animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
    from { box-shadow: 0 0 20px rgba(37, 99, 235, 0.5); }
    to { box-shadow: 0 0 30px rgba(37, 99, 235, 0.8); }
}
`;

// Inject additional CSS
const additionalStyle = document.createElement('style');
additionalStyle.textContent = additionalCSS;
document.head.appendChild(additionalStyle);

// Authentication functions
function initializeAuth() {
    // Check if user is logged in on page load
    checkAuthStatus();
    
    // Update navbar based on auth status
    updateNavbarAuth();
    
    // Handle logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

// Check authentication status
function checkAuthStatus() {
    const token = localStorage.getItem('accessToken');
    const user = localStorage.getItem('user');
    
    if (token && user) {
        // Verify token is still valid
        fetch('/api/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Token invalid');
            }
        })
        .then(userData => {
            // Update stored user data
            localStorage.setItem('user', JSON.stringify(userData));
            updateNavbarAuth();
        })
        .catch(error => {
            // Token is invalid, clear storage
            clearAuthData();
            updateNavbarAuth();
        });
    } else {
        updateNavbarAuth();
    }
}

// Update navbar based on authentication status
function updateNavbarAuth() {
    const user = JSON.parse(localStorage.getItem('user'));
    const userMenu = document.querySelector('.navbar .dropdown[sec\\:authorize="isAuthenticated()"]');
    const loginLinks = document.querySelectorAll('.navbar a[href="/login"], .navbar a[href="/register"]');
    
    if (user) {
        // User is logged in
        if (userMenu) {
            userMenu.style.display = 'block';
            const userNameSpan = userMenu.querySelector('span[sec\\:authentication="name"]');
            if (userNameSpan) {
                userNameSpan.textContent = user.fullName || user.username;
            }
        }
        
        // Hide login/register links
        loginLinks.forEach(link => {
            link.parentElement.style.display = 'none';
        });
        
    } else {
        // User is not logged in
        if (userMenu) {
            userMenu.style.display = 'none';
        }
        
        // Show login/register links
        loginLinks.forEach(link => {
            link.parentElement.style.display = 'block';
        });
    }
}

// Logout function
function logout() {
    // Call logout API
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
    })
    .then(() => {
        // Clear local storage
        clearAuthData();
        
        // Show success message
        showAlert('success', 'Đăng xuất thành công!');
        
        // Redirect to home page
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
    })
    .catch(error => {
        console.error('Logout error:', error);
        // Clear local storage anyway
        clearAuthData();
        window.location.href = '/';
    });
}

// Clear authentication data
function clearAuthData() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
}

// Add to cart function with authentication check
function addToCart(productId, quantity = 1) {
    const token = localStorage.getItem('accessToken');
    
    if (!token) {
        showAlert('warning', 'Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng.');
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
        return;
    }
    
    showLoadingSpinner();
    
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            productId: productId,
            quantity: quantity
        })
    })
    .then(response => {
        if (response.status === 401) {
            // Token expired, redirect to login
            clearAuthData();
            showAlert('warning', 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
            return;
        }
        return response.json();
    })
    .then(data => {
        hideLoadingSpinner();
        
        if (data && data.success) {
            // Update cart badge
            updateCartBadge(data.cartItemCount);
            
            // Show success message
            showAlert('success', 'Đã thêm sản phẩm vào giỏ hàng!');
            
            // Animate cart badge
            animateCartBadge();
        } else if (data) {
            showAlert('danger', data.message || 'Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng.');
        }
    })
    .catch(error => {
        hideLoadingSpinner();
        console.error('Error adding to cart:', error);
        showAlert('danger', 'Có lỗi xảy ra. Vui lòng thử lại.');
    });
}

// Add JWT token to all requests
function addAuthHeaderToRequests() {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
        // Override fetch to automatically include Authorization header
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            options.headers = options.headers || {};
            options.headers['Authorization'] = `Bearer ${token}`;
            return originalFetch(url, options);
        };
        
        // Add token to all form submissions
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', function() {
                const tokenInput = document.createElement('input');
                tokenInput.type = 'hidden';
                tokenInput.name = 'Authorization';
                tokenInput.value = `Bearer ${token}`;
                this.appendChild(tokenInput);
            });
        });
        
        // Add token to all AJAX requests
        const originalXHROpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
            this.addEventListener('readystatechange', function() {
                if (this.readyState === 1) {
                    this.setRequestHeader('Authorization', `Bearer ${token}`);
                }
            });
            return originalXHROpen.apply(this, arguments);
        };
    }
}

// Initialize authentication when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeAuth();
    addAuthHeaderToRequests();
    
    // Add token to all page navigations
    addTokenToPageRequests();
});

// Add token to page navigation requests
function addTokenToPageRequests() {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
        // Override all link clicks to add token
        document.addEventListener('click', function(e) {
            const link = e.target.closest('a');
            if (link && link.href && !link.href.startsWith('javascript:') && 
                !link.href.includes('#') && !link.target === '_blank') {
                
                const url = new URL(link.href);
                // Only add token for same-origin requests
                if (url.origin === window.location.origin) {
                    e.preventDefault();
                    
                    // Navigate with token in header
                    fetch(link.href, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'X-Auth-Token': token
                        },
                        credentials: 'same-origin'
                    })
                    .then(response => {
                        if (response.ok) {
                            return response.text();
                        } else if (response.status === 401) {
                            // Token expired, redirect to login
                            clearAuthData();
                            window.location.href = '/login';
                            return;
                        }
                        throw new Error('Navigation failed');
                    })
                    .then(html => {
                        if (html) {
                            // Update page content
                            document.open();
                            document.write(html);
                            document.close();
                            
                            // Update URL
                            history.pushState(null, '', link.href);
                        }
                    })
                    .catch(error => {
                        console.error('Navigation error:', error);
                        // Fallback to normal navigation
                        window.location.href = link.href;
                    });
                }
            }
        });
        
        // Handle browser back/forward
        window.addEventListener('popstate', function(e) {
            location.reload();
        });
    }
}