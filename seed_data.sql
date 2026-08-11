-- Fresh Food DB - Seed Data
-- Run this in your Supabase SQL Editor

-- 1. Insert Brands
INSERT INTO public.brands (name) VALUES 
('Soummam'), 
('Hodna'), 
('Danone'), 
('Palmary'), 
('Candia')
ON CONFLICT DO NOTHING;

-- 2. Insert Categories
INSERT INTO public.categories (name) VALUES 
('Yogurt'), 
('Milk'), 
('Cheese'), 
('Juice'), 
('Dessert')
ON CONFLICT DO NOTHING;

-- 3. Insert Products
INSERT INTO public.products (name, barcode, selling_price, purchase_price, min_stock, current_stock) VALUES
('Soummam Yogurt Vanilla 125g', '8012345678901', 25.00, 18.00, 50, 200),
('Soummam Yogurt Strawberry 125g', '8012345678902', 25.00, 18.00, 50, 150),
('Candia Milk 1L', '8012345678903', 110.00, 95.00, 100, 500),
('Hodna Camembert 250g', '8012345678904', 350.00, 290.00, 20, 45),
('Danone Activia 125g', '8012345678905', 40.00, 32.00, 30, 120),
('Palmary Chocolate Dessert', '8012345678906', 60.00, 45.00, 40, 80),
('Soummam Fort Milk Chocolate 200ml', '8012345678907', 45.00, 35.00, 100, 300),
('Fresh Gouda Cheese 1Kg', '8012345678908', 1800.00, 1400.00, 5, 12),
('Orange Juice 1L', '8012345678909', 150.00, 110.00, 30, 60),
('Butter 200g', '8012345678910', 200.00, 160.00, 40, 10)
ON CONFLICT (barcode) DO NOTHING;

-- 4. Insert Stock Batches (to make FEFO work)
DO $$ 
DECLARE 
    product_record RECORD;
BEGIN
    FOR product_record IN SELECT id, current_stock FROM public.products LOOP
        INSERT INTO public.stock_batches (product_id, quantity, expiration_date)
        VALUES (
            product_record.id, 
            product_record.current_stock, 
            CURRENT_DATE + INTERVAL '14 days'
        );
    END LOOP;
END $$;

-- 5. Insert Customers
INSERT INTO public.customers (name, phone, customer_type, credit_limit, current_credit) VALUES
('Supermarket Algiers Center', '0555123456', 'WHOLESALE', 100000.00, 15000.00),
('Local Grocer Said', '0777123456', 'RETAIL', 20000.00, 5000.00),
('Restaurant La Mer', '0666123456', 'WHOLESALE', 50000.00, 0.00),
('Mini Market Zeralda', '0555987654', 'RETAIL', 10000.00, 8500.00),
('Cafeteria Universitaire', '0777987654', 'WHOLESALE', 30000.00, 12000.00)
ON CONFLICT DO NOTHING;

-- Done!
