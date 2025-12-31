-- Create news categories table
CREATE TABLE IF NOT EXISTS news_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_news_category_name (name),
    INDEX idx_news_category_slug (slug),
    INDEX idx_news_category_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default news categories
INSERT INTO news_categories (name, slug, description, sort_order) VALUES
('Khuyến mãi', 'khuyen-mai', 'Tin tức về các chương trình khuyến mãi và ưu đãi', 1),
('Sự kiện', 'su-kien', 'Thông tin về các sự kiện và hoạt động của công ty', 2),
('Đặc sản', 'dac-san', 'Giới thiệu về các sản phẩm đặc sản địa phương', 3),
('Tin tức chung', 'tin-tuc-chung', 'Các tin tức và thông báo chung', 4);