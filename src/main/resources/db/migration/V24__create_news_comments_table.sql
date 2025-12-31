-- Create news comments table
CREATE TABLE IF NOT EXISTS news_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    guest_name VARCHAR(100),
    guest_email VARCHAR(100),
    content TEXT NOT NULL,
    parent_id BIGINT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (article_id) REFERENCES news_articles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES news_comments(id) ON DELETE CASCADE,
    
    INDEX idx_comment_article (article_id),
    INDEX idx_comment_status (status),
    INDEX idx_comment_created_at (created_at),
    INDEX idx_comment_parent (parent_id),
    INDEX idx_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;