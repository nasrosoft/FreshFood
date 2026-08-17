-- ==========================================
-- FIX: Create Purchases Tables & RPC
-- ==========================================

-- 1. Create purchases table
CREATE TABLE IF NOT EXISTS purchases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id UUID REFERENCES suppliers(id) ON DELETE SET NULL,
    invoice_number TEXT,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

-- 2. Create purchase_items table
CREATE TABLE IF NOT EXISTS purchase_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_id UUID REFERENCES purchases(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    batch_id UUID REFERENCES stock_batches(id) ON DELETE SET NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(12, 2) DEFAULT 0,
    expiration_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Ensure columns exist and constraints are relaxed if tables were already partially created previously
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS supplier_id UUID REFERENCES suppliers(id) ON DELETE SET NULL;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS invoice_number TEXT;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL;

ALTER TABLE purchase_items ADD COLUMN IF NOT EXISTS batch_id UUID REFERENCES stock_batches(id) ON DELETE SET NULL;
ALTER TABLE purchase_items ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1;
ALTER TABLE purchase_items ADD COLUMN IF NOT EXISTS purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE purchase_items ADD COLUMN IF NOT EXISTS subtotal DECIMAL(12, 2) DEFAULT 0;
ALTER TABLE purchase_items ADD COLUMN IF NOT EXISTS expiration_date DATE;

-- Relax NOT NULL constraint on subtotal / unit_price if they existed in the old schema
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'purchase_items' AND column_name = 'subtotal'
    ) THEN
        ALTER TABLE purchase_items ALTER COLUMN subtotal DROP NOT NULL;
        ALTER TABLE purchase_items ALTER COLUMN subtotal SET DEFAULT 0;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'purchase_items' AND column_name = 'unit_price'
    ) THEN
        ALTER TABLE purchase_items ALTER COLUMN unit_price DROP NOT NULL;
        ALTER TABLE purchase_items ALTER COLUMN unit_price SET DEFAULT 0;
    END IF;
END $$;

-- 4. Update stock_batches table with missing columns from local db
ALTER TABLE stock_batches ADD COLUMN IF NOT EXISTS batch_number TEXT;
ALTER TABLE stock_batches ADD COLUMN IF NOT EXISTS purchase_price DECIMAL(12,2) DEFAULT 0;

-- 5. Enable RLS and add Policies
ALTER TABLE purchases ENABLE ROW LEVEL SECURITY;
ALTER TABLE purchase_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Enable read access for all authenticated users" ON purchases;
DROP POLICY IF EXISTS "Enable insert for authenticated users" ON purchases;
DROP POLICY IF EXISTS "Enable update for authenticated users" ON purchases;
DROP POLICY IF EXISTS "Enable delete for authenticated users" ON purchases;

CREATE POLICY "Enable read access for all authenticated users" ON purchases FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Enable insert for authenticated users" ON purchases FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Enable update for authenticated users" ON purchases FOR UPDATE USING (auth.role() = 'authenticated');
CREATE POLICY "Enable delete for authenticated users" ON purchases FOR DELETE USING (auth.role() = 'authenticated');

DROP POLICY IF EXISTS "Enable read access for all authenticated users" ON purchase_items;
DROP POLICY IF EXISTS "Enable insert for authenticated users" ON purchase_items;
DROP POLICY IF EXISTS "Enable update for authenticated users" ON purchase_items;
DROP POLICY IF EXISTS "Enable delete for authenticated users" ON purchase_items;

CREATE POLICY "Enable read access for all authenticated users" ON purchase_items FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Enable insert for authenticated users" ON purchase_items FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Enable update for authenticated users" ON purchase_items FOR UPDATE USING (auth.role() = 'authenticated');
CREATE POLICY "Enable delete for authenticated users" ON purchase_items FOR DELETE USING (auth.role() = 'authenticated');

-- 6. Create process_purchase RPC function
-- Drop previous versions to avoid conflicts
DROP FUNCTION IF EXISTS process_purchase(JSONB);
DROP FUNCTION IF EXISTS process_purchase(UUID, UUID, DECIMAL, JSONB, TEXT, UUID, TEXT);

-- Accepts exact named parameters matching the Android PurchaseRequest object
CREATE OR REPLACE FUNCTION process_purchase(
    id UUID,
    user_id UUID,
    total_amount DECIMAL,
    items JSONB,
    invoice_number TEXT DEFAULT NULL,
    supplier_id UUID DEFAULT NULL,
    created_at TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_item JSONB;
    v_product_id UUID;
    v_qty INT;
    v_purchase_price DECIMAL(12,2);
    v_expiration_date DATE;
    v_batch_id UUID;
    v_actual_user_id UUID := user_id;
BEGIN
    -- Handle mock user ID from Android App development to prevent foreign key errors
    IF v_actual_user_id = '00000000-0000-0000-0000-000000000000'::UUID THEN
        v_actual_user_id := auth.uid(); -- Will be NULL if not authenticated
    END IF;

    -- Insert into purchases table
    INSERT INTO purchases (id, supplier_id, invoice_number, total_amount, status, user_id)
    VALUES (id, supplier_id, invoice_number, COALESCE(total_amount, 0), 'COMPLETED', v_actual_user_id);

    -- Loop through items
    FOR v_item IN SELECT * FROM jsonb_array_elements(items)
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_qty := (v_item->>'quantity')::INT;
        v_purchase_price := COALESCE((v_item->>'purchase_price')::DECIMAL, 0);
        v_expiration_date := NULLIF(v_item->>'expiration_date', '')::DATE;
        
        -- Generate a new batch ID
        v_batch_id := uuid_generate_v4();
        
        -- Insert new stock batch
        INSERT INTO stock_batches (id, product_id, batch_number, expiration_date, quantity, purchase_price)
        VALUES (
            v_batch_id, 
            v_product_id, 
            'BATCH-' || to_char(NOW(), 'YYYYMMDD') || '-' || upper(substring(v_batch_id::text from 1 for 4)), 
            COALESCE(v_expiration_date, CURRENT_DATE), 
            v_qty, 
            v_purchase_price
        );
        
        -- Insert purchase item (including subtotal calculation)
        INSERT INTO purchase_items (purchase_id, product_id, batch_id, quantity, purchase_price, subtotal, expiration_date)
        VALUES (id, v_product_id, v_batch_id, v_qty, v_purchase_price, (v_qty * v_purchase_price), v_expiration_date);
        
        -- Update product's current stock and latest purchase price
        UPDATE products 
        SET current_stock = current_stock + v_qty,
            purchase_price = v_purchase_price
        WHERE products.id = v_product_id;
        
        -- Add to stock movements
        INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
        VALUES (v_product_id, v_batch_id, v_qty, 'PURCHASE', id, v_actual_user_id);
    END LOOP;

    RETURN jsonb_build_object('success', true, 'purchase_id', id);
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Failed to process purchase: %', SQLERRM;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
