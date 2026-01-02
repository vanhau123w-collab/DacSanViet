-- Ensure user_id can be null in orders table for guest checkout
-- This migration ensures the column is nullable even if previous migrations failed

ALTER TABLE orders MODIFY COLUMN user_id BIGINT NULL;

-- Update foreign key constraint to handle null values properly
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FKel9kyl84ego2otj2accfd8mr7;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK32ql8ubntj5uh44ph9659tiih;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS orders_ibfk_1;

-- Add the foreign key constraint back with proper null handling
ALTER TABLE orders ADD CONSTRAINT FK_orders_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;