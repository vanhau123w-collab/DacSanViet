-- ===================================================================
-- ĐẶCSAN VIỆT - INITIAL DATABASE SCHEMA
-- Version: 1.0.0
-- Description: Complete database schema for Vietnamese specialty e-commerce platform
-- ===================================================================

-- ===================================================================
-- 1. USERS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role ENUM('USER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
);

-- ===================================================================
-- 2. CATEGORIES TABLE (2-level hierarchy)
-- ===================================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    parent_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_parent (parent_id),
    INDEX idx_active (is_active)
);

-- ===================================================================
-- 3. SUPPLIERS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    tax_code VARCHAR(50),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_active (is_active)
);

-- ===================================================================
-- 4. PRODUCTS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    story TEXT,
    price DECIMAL(12,2) NOT NULL,
    sale_price DECIMAL(12,2),
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    category_id BIGINT,
    supplier_id BIGINT,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_featured (is_featured),
    INDEX idx_active (is_active),
    INDEX idx_price (price)
);

-- ===================================================================
-- 5. PRODUCT IMAGES TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id),
    INDEX idx_primary (is_primary),
    INDEX idx_order (display_order)
);

-- ===================================================================
-- 6. ORDERS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NULL,
    guest_email VARCHAR(100),
    guest_name VARCHAR(100),
    guest_phone VARCHAR(20),
    
    -- Shipping Information
    shipping_name VARCHAR(100) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_email VARCHAR(100),
    shipping_address TEXT NOT NULL,
    shipping_city VARCHAR(100),
    shipping_district VARCHAR(100),
    shipping_ward VARCHAR(100),
    shipping_method ENUM('STANDARD', 'EXPRESS_5H') NOT NULL DEFAULT 'STANDARD',
    shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    shipping_carrier VARCHAR(100),
    tracking_number VARCHAR(100),
    
    -- Order Details
    subtotal DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('COD', 'VNPAY', 'MOMO', 'VIETQR') NOT NULL DEFAULT 'COD',
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    order_status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    
    -- Additional Information
    notes TEXT,
    admin_notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_order_number (order_number),
    INDEX idx_user (user_id),
    INDEX idx_guest_email (guest_email),
    INDEX idx_status (order_status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created (created_at)
);

-- ===================================================================
-- 7. ORDER ITEMS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    image_url VARCHAR(500),
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
);

-- ===================================================================
-- 8. PRODUCT REVIEWS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS product_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT,
    reviewer_name VARCHAR(100) NOT NULL,
    reviewer_email VARCHAR(100),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    comment TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_product (product_id),
    INDEX idx_user (user_id),
    INDEX idx_rating (rating),
    INDEX idx_approved (is_approved)
);

-- ===================================================================
-- 9. PRODUCT Q&A TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS product_qa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT,
    questioner_name VARCHAR(100) NOT NULL,
    questioner_email VARCHAR(100),
    question TEXT NOT NULL,
    answer TEXT,
    answered_by BIGINT,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    answered_at TIMESTAMP NULL,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (answered_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_product (product_id),
    INDEX idx_user (user_id),
    INDEX idx_public (is_public)
);

-- ===================================================================
-- 10. PROMOTIONS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    code VARCHAR(50) UNIQUE,
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(12,2),
    max_discount_amount DECIMAL(12,2),
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_dates (start_date, end_date)
);

-- ===================================================================
-- 11. NEWS CATEGORIES TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS news_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_slug (slug),
    INDEX idx_active (is_active)
);

-- ===================================================================
-- 12. NEWS ARTICLES TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    summary TEXT,
    content LONGTEXT NOT NULL,
    image_url VARCHAR(500),
    category_id BIGINT,
    author_id BIGINT,
    view_count INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES news_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_title (title),
    INDEX idx_slug (slug),
    INDEX idx_category (category_id),
    INDEX idx_author (author_id),
    INDEX idx_published (is_published),
    INDEX idx_featured (is_featured),
    INDEX idx_published_at (published_at)
);

-- ===================================================================
-- 13. NEWS COMMENTS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS news_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_id BIGINT NOT NULL,
    user_id BIGINT,
    commenter_name VARCHAR(100) NOT NULL,
    commenter_email VARCHAR(100),
    comment TEXT NOT NULL,
    parent_id BIGINT,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES news_comments(id) ON DELETE CASCADE,
    INDEX idx_news (news_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id),
    INDEX idx_approved (is_approved)
);

-- ===================================================================
-- 14. CHAT MESSAGES TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    user_id BIGINT,
    sender_name VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    sender_type ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_session (session_id),
    INDEX idx_user (user_id),
    INDEX idx_sender_type (sender_type),
    INDEX idx_read (is_read),
    INDEX idx_created (created_at)
);

-- ===================================================================
-- INITIAL DATA INSERTION
-- ===================================================================

-- Note: Initial users will be created by DataInitializer.java
-- This ensures proper password encoding and prevents duplicate entries

-- Insert main categories (3 regions)
INSERT IGNORE INTO categories (id, name, description, is_active) VALUES
(1, 'Đặc Sản Miền Bắc', 'Các sản phẩm đặc sản từ miền Bắc Việt Nam', TRUE),
(2, 'Đặc Sản Miền Trung', 'Các sản phẩm đặc sản từ miền Trung Việt Nam', TRUE),
(3, 'Đặc Sản Miền Nam', 'Các sản phẩm đặc sản từ miền Nam Việt Nam', TRUE);

-- Insert province subcategories
INSERT IGNORE INTO categories (name, description, parent_id, is_active) VALUES
-- Miền Bắc
('Hà Nội', 'Đặc sản từ thủ đô Hà Nội - Bánh chưng, bánh giầy, chả cá Lã Vọng', 1, TRUE),
('Hải Phòng', 'Đặc sản từ thành phố cảng Hải Phòng - Bánh đa cua, nem cua bể', 1, TRUE),
('Quảng Ninh', 'Đặc sản từ Quảng Ninh - Ngọc trai, hải sản tươi sống', 1, TRUE),
('Thái Nguyên', 'Đặc sản từ Thái Nguyên - Chè Thái Nguyên, cốm xanh', 1, TRUE),

-- Miền Trung
('Thừa Thiên Huế', 'Đặc sản từ Huế - Bún bò Huế, bánh khoái, chè Huế', 2, TRUE),
('Đà Nẵng', 'Đặc sản từ Đà Nẵng - Mì Quảng, bánh tráng cuốn thịt heo', 2, TRUE),
('Quảng Nam', 'Đặc sản từ Quảng Nam - Mì Quảng, bánh xèo, cao lầu', 2, TRUE),
('Khánh Hòa', 'Đặc sản từ Khánh Hòa - Bánh căn, nem nướng Nha Trang', 2, TRUE),

-- Miền Nam
('TP. Hồ Chí Minh', 'Đặc sản từ TP.HCM - Bánh mì, hủ tiếu, bánh xèo', 3, TRUE),
('Cần Thơ', 'Đặc sản từ Cần Thơ - Bánh cống, bánh xèo', 3, TRUE),
('An Giang', 'Đặc sản từ An Giang - Bánh pía, bánh tét', 3, TRUE),
('Bến Tre', 'Đặc sản từ Bến Tre - Kẹo dừa, bánh tráng dừa', 3, TRUE);

-- Insert default news category
INSERT IGNORE INTO news_categories (name, description, slug, is_active) VALUES
('Tin Tức Chung', 'Tin tức tổng hợp về đặc sản Việt Nam', 'tin-tuc-chung', TRUE),
('Văn Hóa Ẩm Thực', 'Những câu chuyện về văn hóa ẩm thực Việt', 'van-hoa-am-thuc', TRUE),
('Hướng Dẫn Chế Biến', 'Cách chế biến các món đặc sản', 'huong-dan-che-bien', TRUE);

-- ===================================================================
-- END OF INITIAL SCHEMA
-- ===================================================================