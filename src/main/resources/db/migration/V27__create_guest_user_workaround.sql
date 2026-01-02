-- Create a default guest user as workaround for user_id constraint
-- This allows guest orders to reference a valid user_id

INSERT IGNORE INTO users (id, username, email, password, full_name, phone, role, created_at, updated_at) 
VALUES (0, 'guest', 'guest@system.local', '$2a$10$dummy.hash.for.guest.user.account', 'Guest User', '0000000000', 'USER', NOW(), NOW());

-- Alternative: Try to modify the orders table again with a different approach
ALTER TABLE orders MODIFY user_id BIGINT NULL;