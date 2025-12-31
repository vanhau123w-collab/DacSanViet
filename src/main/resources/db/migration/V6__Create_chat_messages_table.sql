-- Create chat_messages table for chatbox functionality
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    sender_name VARCHAR(100),
    sender_email VARCHAR(100),
    message TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    admin_id BIGINT,
    
    INDEX idx_session_id (session_id),
    INDEX idx_message_type (message_type),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read),
    INDEX idx_admin_id (admin_id)
);

-- Add some sample data for testing
INSERT INTO chat_messages (session_id, message, message_type, is_read) VALUES
('chat_sample123', 'Xin chào! Chúng tôi có thể giúp gì cho bạn hôm nay?', 'SYSTEM', true);