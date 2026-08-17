-- 1. Drop existing app_settings if it had a different schema
DROP TABLE IF EXISTS app_settings CASCADE;

-- 2. Create app_settings table with the required columns
CREATE TABLE app_settings (
    id INT PRIMARY KEY DEFAULT 1,
    app_enabled INT NOT NULL DEFAULT 1 CHECK (app_enabled IN (0, 1))
);

-- 3. Insert default row (1 = active)
INSERT INTO app_settings (id, app_enabled)
VALUES (1, 1);

-- 4. Enable Row Level Security and grant public read access
ALTER TABLE app_settings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow read access to app_settings" ON app_settings;
CREATE POLICY "Allow read access to app_settings"
ON app_settings FOR SELECT
USING (true);
