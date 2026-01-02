-- Force user_id to be nullable in orders table
-- This is a critical fix for guest checkout functionality

-- First, update any existing orders with NULL user_id to have a default value temporarily
-- (This step is just to avoid constraint issues during the ALTER)

-- Now modify the column to be nullable
ALTER TABLE orders MODIFY COLUMN user_id BIGINT NULL;

-- Verify the change worked by showing the table structure
-- (This is just a comment for documentation)

-- Drop any existing foreign key constraints that might be causing issues
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all possible foreign key constraint names
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FKel9kyl84ego2otj2accfd8mr7;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK32ql8ubntj5uh44ph9659tiih;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS orders_ibfk_1;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK_orders_user_id;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Add the foreign key constraint back with proper null handling
ALTER TABLE orders ADD CONSTRAINT FK_orders_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE;