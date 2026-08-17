-- Add image and emoji columns to products table if they do not exist
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_source TEXT DEFAULT 'emoji';
ALTER TABLE products ADD COLUMN IF NOT EXISTS emoji TEXT DEFAULT '📦';
