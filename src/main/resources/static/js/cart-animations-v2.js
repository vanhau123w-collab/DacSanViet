/**
 * Cart Animations V2 - Simple & Clean
 * Dropdown style attached to cart icon
 */

(function() {
    'use strict';
    
    // Add CSS
    const style = document.createElement('style');
    style.textContent = `
        /* Product bubble animation - Rocket style */
        @keyframes floatToCart {
            0% {
                transform: translate(0, 0) scale(1) rotate(0deg);
                opacity: 1;
            }
            50% {
                transform: translate(calc(var(--tx) * 0.5), calc(var(--ty) * 0.5)) scale(0.7) rotate(-15deg);
                opacity: 0.9;
            }
            100% {
                transform: translate(var(--tx), var(--ty)) scale(0.2) rotate(-30deg);
                opacity: 0;
            }
        }
        
        .floating-product-bubble {
            position: fixed;
            z-index: 999999;
            pointer-events: none;
            animation: floatToCart 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
            will-change: transform, opacity;
            filter: drop-shadow(0 0 10px rgba(78, 194, 182, 0.6));
        }
        
        /* Ensure header has lower z-index */
        header, .modern-header, .navbar {
            z-index: 1000 !important;
        }
        
        .floating-product-bubble img {
            width: 100px;
            height: 100px;
            object-fit: cover;
            border-radius: 50%;
            border: 4px solid #4ec2b6;
            box-shadow: 0 6px 20px rgba(78, 194, 182, 0.8), 
                        0 0 30px rgba(78, 194, 182, 0.4),
                        inset 0 0 20px rgba(255, 255, 255, 0.3);
            background: white;
            display: block;
        }
        
        /* Smoke trail particles */
        @keyframes smokeTrail {
            0% {
                transform: translate(0, 0) scale(1);
                opacity: 0.8;
            }
            100% {
                transform: translate(var(--drift-x), var(--drift-y)) scale(2);
                opacity: 0;
            }
        }
        
        .smoke-particle {
            position: fixed;
            z-index: 999998;
            pointer-events: none;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(78, 194, 182, 0.6) 0%, rgba(78, 194, 182, 0.2) 50%, transparent 100%);
            animation: smokeTrail 0.6s ease-out forwards;
        }
        
        /* Sparkle particles */
        @keyframes sparkle {
            0% {
                transform: translate(0, 0) scale(1);
                opacity: 1;
            }
            100% {
                transform: translate(var(--sparkle-x), var(--sparkle-y)) scale(0);
                opacity: 0;
            }
        }
        
        .sparkle-particle {
            position: fixed;
            z-index: 999998;
            pointer-events: none;
            width: 8px;
            height: 8px;
            background: #4ec2b6;
            border-radius: 50%;
            box-shadow: 0 0 10px #4ec2b6, 0 0 20px #4ec2b6;
            animation: sparkle 0.5s ease-out forwards;
        }
        
        /* Cart dropdown button hover */
        .cart-dropdown-footer a.btn:hover {
            background: linear-gradient(135deg, #8B4513, #a93226) !important;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
        }
        
        /* Cart badge animation */
        @keyframes badgeBounce {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.3); }
        }
        
        .cart-badge-animated {
            animation: badgeBounce 0.5s ease;
        }
        
        /* Cart dropdown */
        .cart-icon-wrapper {
            position: relative;
        }
        
        .cart-dropdown {
            position: absolute;
            top: calc(100% + 0.5rem);
            right: 0;
            width: 320px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            z-index: 1050;
            opacity: 0;
            visibility: hidden;
            transform: translateY(-10px);
            transition: all 0.2s ease;
        }
        
        .cart-dropdown.show {
            opacity: 1;
            visibility: visible;
            transform: translateY(0);
        }
        
        .cart-dropdown-header {
            padding: 0.75rem 1rem;
            border-bottom: 1px solid #e9ecef;
            background: #f8f9fa;
            border-radius: 8px 8px 0 0;
        }
        
        .cart-dropdown-header h6 {
            font-size: 0.875rem;
            font-weight: 600;
            margin: 0;
            color: #333;
        }
        
        .cart-dropdown-body {
            padding: 0.5rem;
            max-height: 300px;
            overflow-y: auto;
        }
        
        .cart-dropdown-item {
            display: flex;
            gap: 0.5rem;
            padding: 0.5rem;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .cart-dropdown-item:last-child {
            border-bottom: none;
        }
        
        .cart-dropdown-item img {
            width: 40px;
            height: 40px;
            object-fit: cover;
            border-radius: 4px;
            flex-shrink: 0;
        }
        
        .cart-dropdown-item-info {
            flex: 1;
            min-width: 0;
        }
        
        .cart-dropdown-item-name {
            font-size: 0.8rem;
            font-weight: 500;
            margin-bottom: 0.25rem;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        
        .cart-dropdown-item-price {
            font-size: 0.75rem;
            color: #4ec2b6;
            font-weight: 600;
        }
        
        .cart-dropdown-footer {
            padding: 0.75rem 1rem;
            border-top: 1px solid #e9ecef;
            background: #fafafa;
            border-radius: 0 0 8px 8px;
        }
        
        .cart-dropdown-total {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.75rem;
            font-weight: 600;
        }
        
        .cart-dropdown-total-price {
            color: #4ec2b6;
            font-size: 1.1rem;
        }
        
        /* Mobile */
        @media (max-width: 768px) {
            .cart-dropdown {
                width: 280px;
            }
        }
    `;
    document.head.appendChild(style);
    
    /**
     * Create product bubble animation
     */
    window.createProductBubbleAnimation = function(sourceElement, productImage) {
        console.log('🎨 Creating bubble animation', { 
            sourceElement, 
            productImage,
            cartIconExists: !!document.querySelector('.cart-icon-wrapper')
        });
        
        const cartIconWrapper = document.querySelector('.cart-icon-wrapper');
        if (!cartIconWrapper) {
            console.error('❌ Cart icon wrapper not found!');
            return;
        }
        
        if (!sourceElement) {
            console.error('❌ Source element not found!');
            return;
        }
        
        // Get the actual icon element (i tag) for precise positioning
        const cartIconElement = cartIconWrapper.querySelector('i.bi-bag');
        const targetRect = cartIconElement ? cartIconElement.getBoundingClientRect() : cartIconWrapper.getBoundingClientRect();
        const sourceRect = sourceElement.getBoundingClientRect();
        
        // Calculate center positions
        const sourceCenterX = sourceRect.left + sourceRect.width / 2;
        const sourceCenterY = sourceRect.top + sourceRect.height / 2;
        const targetCenterX = targetRect.left + targetRect.width / 2;
        const targetCenterY = targetRect.top + targetRect.height / 2;
        
        console.log('📍 Positions:', {
            source: { x: sourceCenterX, y: sourceCenterY },
            target: { x: targetCenterX, y: targetCenterY }
        });
        
        // Calculate translation needed (from bubble center to icon center)
        const tx = targetCenterX - sourceCenterX;
        const ty = targetCenterY - sourceCenterY;
        
        const bubble = document.createElement('div');
        bubble.className = 'floating-product-bubble';
        // Position bubble center at source center
        bubble.style.left = sourceCenterX - 50 + 'px';
        bubble.style.top = sourceCenterY - 50 + 'px';
        bubble.style.setProperty('--tx', tx + 'px');
        bubble.style.setProperty('--ty', ty + 'px');
        
        bubble.innerHTML = `<img src="${productImage}" alt="Product" onerror="this.src='https://via.placeholder.com/100'">`;
        
        document.body.appendChild(bubble);
        console.log('✅ Bubble added to DOM with z-index:', window.getComputedStyle(bubble).zIndex);
        
        // Create smoke trail effect
        createSmokeTrail(sourceCenterX, sourceCenterY, targetCenterX, targetCenterY);
        
        // Create sparkle particles
        createSparkles(sourceCenterX, sourceCenterY);
        
        setTimeout(() => {
            bubble.remove();
            console.log('🗑️ Bubble removed');
        }, 800);
        
        animateCartBadge();
    };
    
    /**
     * Create smoke trail particles
     */
    function createSmokeTrail(startX, startY, endX, endY) {
        const particleCount = 15;
        const duration = 800; // Same as bubble animation
        
        for (let i = 0; i < particleCount; i++) {
            setTimeout(() => {
                const smoke = document.createElement('div');
                smoke.className = 'smoke-particle';
                
                // Calculate current position along the path
                const progress = i / particleCount;
                const currentX = startX + (endX - startX) * progress;
                const currentY = startY + (endY - startY) * progress;
                
                // Random drift
                const driftX = (Math.random() - 0.5) * 50;
                const driftY = (Math.random() - 0.5) * 50;
                
                smoke.style.left = currentX - 10 + 'px';
                smoke.style.top = currentY - 10 + 'px';
                smoke.style.setProperty('--drift-x', driftX + 'px');
                smoke.style.setProperty('--drift-y', driftY + 'px');
                smoke.style.animationDelay = '0s';
                
                document.body.appendChild(smoke);
                
                setTimeout(() => {
                    smoke.remove();
                }, 600);
            }, (duration / particleCount) * i);
        }
    }
    
    /**
     * Create sparkle particles
     */
    function createSparkles(startX, startY) {
        const sparkleCount = 8;
        
        for (let i = 0; i < sparkleCount; i++) {
            setTimeout(() => {
                const sparkle = document.createElement('div');
                sparkle.className = 'sparkle-particle';
                
                // Random direction
                const angle = (Math.PI * 2 * i) / sparkleCount;
                const distance = 30 + Math.random() * 20;
                const sparkleX = Math.cos(angle) * distance;
                const sparkleY = Math.sin(angle) * distance;
                
                sparkle.style.left = startX - 4 + 'px';
                sparkle.style.top = startY - 4 + 'px';
                sparkle.style.setProperty('--sparkle-x', sparkleX + 'px');
                sparkle.style.setProperty('--sparkle-y', sparkleY + 'px');
                
                document.body.appendChild(sparkle);
                
                setTimeout(() => {
                    sparkle.remove();
                }, 500);
            }, i * 30);
        }
    }
    
    /**
     * Animate cart badge
     */
    function animateCartBadge() {
        const badges = document.querySelectorAll('.cart-badge');
        badges.forEach(badge => {
            badge.classList.remove('cart-badge-animated');
            void badge.offsetWidth;
            badge.classList.add('cart-badge-animated');
        });
    }
    
    /**
     * Show cart dropdown
     */
    window.showCartDropdown = function() {
        const wrapper = document.querySelector('.cart-icon-wrapper');
        if (!wrapper) {
            console.error('Cart icon wrapper not found');
            return;
        }
        
        let dropdown = wrapper.querySelector('.cart-dropdown');
        const cart = window.CartManager ? window.CartManager.getCart() : { items: [], total: 0 };
        
        if (cart.items.length === 0) {
            if (dropdown) dropdown.remove();
            return;
        }
        
        if (!dropdown) {
            dropdown = document.createElement('div');
            dropdown.className = 'cart-dropdown';
            dropdown.innerHTML = `
                <div class="cart-dropdown-header">
                    <h6><i class="bi bi-cart3 me-2"></i>Giỏ Hàng</h6>
                </div>
                <div class="cart-dropdown-body"></div>
                <div class="cart-dropdown-footer"></div>
            `;
            wrapper.appendChild(dropdown);
        }
        
        updateDropdownContent(dropdown, cart);
        
        setTimeout(() => {
            dropdown.classList.add('show');
        }, 10);
    };
    
    /**
     * Hide cart dropdown
     */
    window.hideCartDropdown = function() {
        const dropdown = document.querySelector('.cart-dropdown');
        if (dropdown) {
            dropdown.classList.remove('show');
        }
    };
    
    /**
     * Update cart dropdown content (refresh without showing)
     */
    window.updateCartDropdown = function() {
        const wrapper = document.querySelector('.cart-icon-wrapper');
        if (!wrapper) return;
        
        const dropdown = wrapper.querySelector('.cart-dropdown');
        if (!dropdown) return;
        
        const cart = window.CartManager ? window.CartManager.getCart() : { items: [], total: 0 };
        
        // If cart is empty, remove dropdown
        if (cart.items.length === 0) {
            dropdown.remove();
            return;
        }
        
        // Update existing dropdown content
        updateDropdownContent(dropdown, cart);
    };
    
    /**
     * Update dropdown content
     */
    function updateDropdownContent(dropdown, cart) {
        const body = dropdown.querySelector('.cart-dropdown-body');
        const footer = dropdown.querySelector('.cart-dropdown-footer');
        
        const recentItems = cart.items.slice(-5).reverse();
        
        body.innerHTML = recentItems.map(item => `
            <div class="cart-dropdown-item">
                <img src="${item.imageUrl}" alt="${item.productName}" onerror="this.src='https://via.placeholder.com/40'">
                <div class="cart-dropdown-item-info">
                    <div class="cart-dropdown-item-name">${item.productName}</div>
                    <div class="cart-dropdown-item-price">${item.quantity} x ${formatPrice(item.price)}₫</div>
                </div>
            </div>
        `).join('');
        
        footer.innerHTML = `
            <div class="cart-dropdown-total">
                <span>Tổng cộng:</span>
                <span class="cart-dropdown-total-price">${formatPrice(cart.total)}₫</span>
            </div>
            <div class="d-grid">
                <a href="/cart" class="btn btn-sm" style="background: linear-gradient(135deg, #D2691E, #8B4513); color: white; border: none; transition: all 0.3s ease;">
                    <i class="bi bi-cart-check me-1"></i>Xem Giỏ Hàng
                </a>
            </div>
        `;
    }
    
    /**
     * Format price
     */
    function formatPrice(price) {
        return new Intl.NumberFormat('vi-VN').format(price);
    }
    
    /**
     * Setup hover behavior
     */
    function setupHoverBehavior() {
        const wrapper = document.querySelector('.cart-icon-wrapper');
        if (!wrapper) {
            console.warn('⚠️ Cart icon wrapper not found for hover setup');
            return;
        }
        
        console.log('✅ Setting up hover behavior on cart icon');
        
        let hoverTimeout;
        let leaveTimeout;
        
        wrapper.addEventListener('mouseenter', function() {
            console.log('🖱️ Mouse entered cart icon');
            clearTimeout(leaveTimeout);
            hoverTimeout = setTimeout(() => {
                showCartDropdown();
            }, 200);
        });
        
        wrapper.addEventListener('mouseleave', function(e) {
            console.log('🖱️ Mouse left cart icon');
            clearTimeout(hoverTimeout);
            
            // Check if mouse is moving to dropdown
            const dropdown = wrapper.querySelector('.cart-dropdown');
            if (dropdown && dropdown.contains(e.relatedTarget)) {
                return;
            }
            
            leaveTimeout = setTimeout(() => {
                hideCartDropdown();
            }, 300);
        });
        
        // Keep dropdown open when hovering over it
        document.addEventListener('mouseover', function(e) {
            const dropdown = document.querySelector('.cart-dropdown');
            if (dropdown && dropdown.contains(e.target)) {
                clearTimeout(leaveTimeout);
            }
        });
    }
    
    /**
     * Scroll to show header
     */
    window.scrollToShowHeader = function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    };
    
    // Aliases for backward compatibility
    window.showCartSummaryModal = window.showCartDropdown;
    window.closeCartSummaryModal = window.hideCartDropdown;
    
    // Setup on page load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setupHoverBehavior);
    } else {
        setupHoverBehavior();
    }
    
    console.log('Cart animations V2 loaded');
    
})();
