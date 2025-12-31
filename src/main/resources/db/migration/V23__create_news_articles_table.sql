-- Create news articles table
CREATE TABLE IF NOT EXISTS news_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    content TEXT,
    excerpt VARCHAR(300),
    featured_image VARCHAR(500),
    thumbnail_image VARCHAR(500),
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    category_id BIGINT,
    author_id BIGINT NOT NULL,
    view_count BIGINT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    meta_description VARCHAR(160),
    meta_keywords VARCHAR(255),
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES news_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_news_status (status),
    INDEX idx_news_published_at (published_at),
    INDEX idx_news_category (category_id),
    INDEX idx_news_featured (is_featured),
    INDEX idx_news_slug (slug),
    INDEX idx_news_author (author_id),
    INDEX idx_news_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;