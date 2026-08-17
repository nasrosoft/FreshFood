-- 1. Create or update app_settings table with brand settings
CREATE TABLE IF NOT EXISTS app_settings (
    id INT PRIMARY KEY DEFAULT 1,
    app_enabled INT NOT NULL DEFAULT 1 CHECK (app_enabled IN (0, 1)),
    brand_name TEXT DEFAULT 'Fresh Dairy',
    brand_tagline TEXT DEFAULT 'Stock & Sales Management'
);

-- 2. Add columns if table already exists
ALTER TABLE app_settings ADD COLUMN IF NOT EXISTS brand_name TEXT DEFAULT 'Fresh Dairy';
ALTER TABLE app_settings ADD COLUMN IF NOT EXISTS brand_tagline TEXT DEFAULT 'Stock & Sales Management';

-- 3. Insert default row (1 = active)
INSERT INTO app_settings (id, app_enabled, brand_name, brand_tagline)
VALUES (1, 1, 'Fresh Dairy', 'Stock & Sales Management')
ON CONFLICT (id) DO UPDATE 
SET brand_name = EXCLUDED.brand_name, brand_tagline = EXCLUDED.brand_tagline
WHERE app_settings.brand_name IS NULL;

-- 4. Enable Row Level Security and grant public read & update access
ALTER TABLE app_settings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow read access to app_settings" ON app_settings;
CREATE POLICY "Allow read access to app_settings"
ON app_settings FOR SELECT
USING (true);

DROP POLICY IF EXISTS "Allow update access to app_settings" ON app_settings;
CREATE POLICY "Allow update access to app_settings"
ON app_settings FOR UPDATE
USING (true)
WITH CHECK (true);
