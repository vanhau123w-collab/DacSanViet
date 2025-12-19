-- Create product_images table for multiple images per product
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate existing product images to product_images table
INSERT INTO product_images (product_id, image_url, display_order, is_primary, alt_text)
SELECT 
    id as product_id,
    image_url,
    0 as display_order,
    TRUE as is_primary,
    name as alt_text
FROM products
WHERE image_url IS NOT NULL AND image_url != '';

-- Add 2 more sample images for each product (for demo)
INSERT INTO product_images (product_id, image_url, display_order, is_primary, alt_text)
SELECT 
    id as product_id,
    image_url as image_url,
    1 as display_order,
    FALSE as is_primary,
    CONCAT(name, ' - Ảnh 2') as alt_text
FROM products
WHERE image_url IS NOT NULL AND image_url != '';

INSERT INTO product_images (product_id, image_url, display_order, is_primary, alt_text)
SELECT 
    id as product_id,
    image_url as image_url,
    2 as display_order,
    FALSE as is_primary,
    CONCAT(name, ' - Ảnh 3') as alt_text
FROM products
WHERE image_url IS NOT NULL AND image_url != '';
