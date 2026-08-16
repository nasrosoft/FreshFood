-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. TABLES & CONSTRAINTS
-- ==========================================

-- app_settings
CREATE TABLE app_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_name TEXT NOT NULL,
    logo_url TEXT,
    address TEXT,
    phone TEXT,
    email TEXT,
    currency TEXT DEFAULT 'DA',
    invoice_prefix TEXT DEFAULT 'INV-',
    expiration_warning_days INT DEFAULT 7,
    low_stock_threshold INT DEFAULT 10,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- profiles (extends auth.users)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone TEXT,
    role TEXT NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'SELLER', 'DELIVERY')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- categories
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- brands
CREATE TABLE brands (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- suppliers
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    phone TEXT,
    address TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- products
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    barcode TEXT UNIQUE,
    name TEXT NOT NULL,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    brand_id UUID REFERENCES brands(id) ON DELETE SET NULL,
    description TEXT,
    image_url TEXT,
    unit TEXT DEFAULT 'Unit',
    purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    selling_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    min_selling_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    current_stock INT NOT NULL DEFAULT 0,
    min_stock INT NOT NULL DEFAULT 0,
    max_stock INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CHECK (current_stock >= 0) -- Stock cannot be negative
);

-- stock_batches (for FEFO tracking)
CREATE TABLE stock_batches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 0,
    expiration_date DATE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CHECK (quantity >= 0)
);

-- stock_movements
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    batch_id UUID REFERENCES stock_batches(id) ON DELETE SET NULL,
    quantity INT NOT NULL,
    movement_type TEXT NOT NULL CHECK (movement_type IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'LOSS', 'EXPIRED', 'TRANSFER', 'DELIVERY_RETURN', 'DELIVERY')),
    reference_id UUID, -- Can refer to a purchase, sale, or return ID
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- customers
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    phone TEXT,
    address TEXT,
    wilaya TEXT,
    commune TEXT,
    photo_url TEXT,
    credit_limit DECIMAL(12, 2) DEFAULT 0,
    current_credit DECIMAL(12, 2) DEFAULT 0,
    customer_type TEXT CHECK (customer_type IN ('RETAIL', 'WHOLESALE', 'SHOP', 'RESTAURANT', 'CAFETERIA', 'OTHER')),
    is_active BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CHECK (current_credit >= 0) -- Credit should not be negative in this system design
);

-- sales
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number TEXT UNIQUE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    credit_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    payment_method TEXT CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER', 'CREDIT', 'PARTIAL_PAYMENT')),
    status TEXT DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'RETURNED')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- sale_items
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID REFERENCES sales(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    batch_id UUID REFERENCES stock_batches(id) ON DELETE SET NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    unit_price DECIMAL(12, 2) NOT NULL,
    cost_price DECIMAL(12, 2) NOT NULL, -- Stored explicitly for historical profit calculation
    subtotal DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- payments
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_method TEXT CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER')),
    reference_id UUID, -- Could be a sale_id or just null for general payment
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- credit_transactions (Ledger to ensure credit calculation is auditable)
CREATE TABLE credit_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_type TEXT CHECK (transaction_type IN ('DEBT', 'PAYMENT')),
    reference_id UUID, -- Sale ID or Payment ID
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- delivery_orders
CREATE TABLE delivery_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    sale_id UUID REFERENCES sales(id) ON DELETE SET NULL,
    delivery_employee_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'PARTIALLY_DELIVERED', 'CANCELLED')),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- delivery_items
CREATE TABLE delivery_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_order_id UUID REFERENCES delivery_orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- audit_logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    action TEXT NOT NULL,
    entity TEXT NOT NULL,
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==========================================
-- 2. TRIGGERS & FUNCTIONS
-- ==========================================

-- Function to handle new user registration
CREATE OR REPLACE FUNCTION public.handle_new_user() 
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, first_name, last_name, role)
  VALUES (new.id, '', '', 'SELLER'); -- Default role
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();


-- Trigger to update customer's current_credit based on credit_transactions
CREATE OR REPLACE FUNCTION update_customer_credit()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.transaction_type = 'DEBT' THEN
        UPDATE customers SET current_credit = current_credit + NEW.amount WHERE id = NEW.customer_id;
    ELSIF NEW.transaction_type = 'PAYMENT' THEN
        UPDATE customers SET current_credit = current_credit - NEW.amount WHERE id = NEW.customer_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trigger_update_customer_credit
AFTER INSERT ON credit_transactions
FOR EACH ROW EXECUTE PROCEDURE update_customer_credit();

-- Trigger to deduct stock when a delivery order is marked as DELIVERED
CREATE OR REPLACE FUNCTION process_delivery_stock()
RETURNS TRIGGER AS $$
DECLARE
    v_item RECORD;
    v_qty_needed INT;
    v_batch RECORD;
    v_qty_to_take INT;
BEGIN
    -- Stock reduction logic is now handled in process_sale immediately upon sale.
    -- We only handle restocking here if the delivery is cancelled.
    IF NEW.status = 'CANCELLED' AND OLD.status != 'CANCELLED' THEN
        -- Restock if it was cancelled
        FOR v_item IN SELECT * FROM delivery_items WHERE delivery_order_id = NEW.id
        LOOP
            UPDATE products SET current_stock = current_stock + v_item.quantity WHERE id = v_item.product_id;
            INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
            VALUES (v_item.product_id, NULL, v_item.quantity, 'DELIVERY_RETURN', NEW.id, NEW.delivery_employee_id);
        END LOOP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trigger_process_delivery_stock
AFTER UPDATE ON delivery_orders
FOR EACH ROW EXECUTE PROCEDURE process_delivery_stock();

-- Trigger to restock and adjust financials when a delivery item is modified by a driver
CREATE OR REPLACE FUNCTION handle_delivery_item_modification()
RETURNS TRIGGER AS $$
DECLARE
    v_diff INT;
    v_sale_id UUID;
    v_customer_id UUID;
    v_user_id UUID;
    v_sale_item RECORD;
    v_unit_price DECIMAL(12,2);
    v_refund_amount DECIMAL(12,2);
BEGIN
    -- Only proceed if it's an UPDATE (quantity reduced) or DELETE
    IF TG_OP = 'DELETE' THEN
        v_diff := OLD.quantity;
    ELSIF TG_OP = 'UPDATE' AND NEW.quantity < OLD.quantity THEN
        v_diff := OLD.quantity - NEW.quantity;
    ELSE
        RETURN COALESCE(NEW, OLD);
    END IF;

    -- Get the delivery order to find sale_id and customer_id
    SELECT sale_id, customer_id, delivery_employee_id INTO v_sale_id, v_customer_id, v_user_id
    FROM delivery_orders WHERE id = OLD.delivery_order_id;

    -- 1. Restock physical item
    UPDATE products SET current_stock = current_stock + v_diff WHERE id = OLD.product_id;
    INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
    VALUES (OLD.product_id, NULL, v_diff, 'DELIVERY_RETURN', OLD.delivery_order_id, v_user_id);

    -- 2. If linked to a sale, adjust financial records
    IF v_sale_id IS NOT NULL THEN
        -- Find the corresponding sale_item
        SELECT * INTO v_sale_item FROM sale_items 
        WHERE sale_id = v_sale_id AND product_id = OLD.product_id LIMIT 1;
        
        IF FOUND THEN
            v_unit_price := v_sale_item.unit_price;
            v_refund_amount := v_diff * v_unit_price;

            -- Update sale_items
            UPDATE sale_items 
            SET quantity = quantity - v_diff, 
                subtotal = subtotal - v_refund_amount
            WHERE id = v_sale_item.id;

            -- UPDATE sales table
            UPDATE sales 
            SET total_amount = total_amount - v_refund_amount
            WHERE id = v_sale_id;
            
            -- Insert a PAYMENT credit transaction to reduce customer's debt for the returned items
            IF v_customer_id IS NOT NULL THEN
                INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
                VALUES (v_customer_id, v_refund_amount, 'PAYMENT', v_sale_id, v_user_id);
            END IF;
        END IF;
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trigger_delivery_item_modified
AFTER UPDATE OR DELETE ON delivery_items
FOR EACH ROW EXECUTE PROCEDURE handle_delivery_item_modification();


-- FEFO RPC: Process a sale automatically handling batches
-- `sale_data` JSON structure:
-- {
--    "customer_id": "uuid", (nullable)
--    "user_id": "uuid",
--    "total_amount": 1000,
--    "paid_amount": 1000,
--    "credit_amount": 0,
--    "payment_method": "CASH",
--    "items": [
--       { "product_id": "uuid", "quantity": 10, "unit_price": 50 }
--    ]
-- }
CREATE OR REPLACE FUNCTION process_sale(sale_data JSONB)
RETURNS JSONB AS $$
DECLARE
    v_sale_id UUID;
    v_invoice_number TEXT;
    v_customer_id UUID;
    v_user_id UUID;
    v_item JSONB;
    v_product_id UUID;
    v_qty_needed INT;
    v_unit_price DECIMAL(12,2);
    v_batch RECORD;
    v_qty_to_take INT;
    v_cost_price DECIMAL(12,2);
    v_delivery_id UUID;
BEGIN
    -- Generate simple invoice number (in production, might need sequences)
    v_invoice_number := 'INV-' || to_char(NOW(), 'YYYYMMDD-HH24MISS');
    v_customer_id := (sale_data->>'customer_id')::UUID;
    v_user_id := (sale_data->>'user_id')::UUID;

    -- 1. Insert Sale record
    INSERT INTO sales (
        invoice_number, customer_id, user_id, total_amount, paid_amount, credit_amount, payment_method, status
    ) VALUES (
        v_invoice_number,
        v_customer_id,
        v_user_id,
        (sale_data->>'total_amount')::DECIMAL,
        (sale_data->>'paid_amount')::DECIMAL,
        (sale_data->>'credit_amount')::DECIMAL,
        sale_data->>'payment_method',
        'COMPLETED'
    ) RETURNING id INTO v_sale_id;

    -- 2. Process Items and FEFO batches
    FOR v_item IN SELECT * FROM jsonb_array_elements(sale_data->'items')
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_qty_needed := (v_item->>'quantity')::INT;
        v_unit_price := (v_item->>'unit_price')::DECIMAL;
        
        -- Get product purchase price as default cost if batches run out somehow
        SELECT purchase_price INTO v_cost_price FROM products WHERE id = v_product_id;

        -- Always deduct stock, even for deliveries, because the physical stock is reserved/leaves the store
        IF TRUE THEN
            -- Iterate over batches ordered by expiration date (FEFO)
            FOR v_batch IN 
                SELECT id, quantity FROM stock_batches 
                WHERE product_id = v_product_id AND quantity > 0
                ORDER BY expiration_date ASC
            LOOP
                IF v_qty_needed <= 0 THEN
                    EXIT; -- Fully satisfied
                END IF;

                IF v_batch.quantity >= v_qty_needed THEN
                    v_qty_to_take := v_qty_needed;
                ELSE
                    v_qty_to_take := v_batch.quantity;
                END IF;

                -- Deduct from batch
                UPDATE stock_batches SET quantity = quantity - v_qty_to_take WHERE id = v_batch.id;
                
                -- Deduct from product global stock
                UPDATE products SET current_stock = current_stock - v_qty_to_take WHERE id = v_product_id;

                -- Record stock movement
                INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
                VALUES (v_product_id, v_batch.id, -v_qty_to_take, 'SALE', v_sale_id, v_user_id);

                -- Record sale item
                INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
                VALUES (v_sale_id, v_product_id, v_batch.id, v_qty_to_take, v_unit_price, v_cost_price, v_qty_to_take * v_unit_price);

                v_qty_needed := v_qty_needed - v_qty_to_take;
            END LOOP;

            -- If still qty needed, it means we oversold.
            -- We will allow it but deduct from product stock so it goes negative, alerting admin.
            IF v_qty_needed > 0 THEN
                UPDATE products SET current_stock = current_stock - v_qty_needed WHERE id = v_product_id;
                INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
                VALUES (v_product_id, NULL, -v_qty_needed, 'SALE', v_sale_id, v_user_id);
                INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
                VALUES (v_sale_id, v_product_id, NULL, v_qty_needed, v_unit_price, v_cost_price, v_qty_needed * v_unit_price);
            END IF;
        END IF;
    END LOOP;

    -- 3. Handle Credit & Payment records
    IF (sale_data->>'credit_amount')::DECIMAL > 0 AND v_customer_id IS NOT NULL THEN
        INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
        VALUES (v_customer_id, (sale_data->>'credit_amount')::DECIMAL, 'DEBT', v_sale_id, v_user_id);
    END IF;

    IF (sale_data->>'paid_amount')::DECIMAL > 0 AND v_customer_id IS NOT NULL THEN
        INSERT INTO payments (customer_id, amount, payment_method, reference_id, user_id)
        VALUES (v_customer_id, (sale_data->>'paid_amount')::DECIMAL, sale_data->>'payment_method', v_sale_id, v_user_id);
    END IF;

    -- 4. Handle Delivery Creation
    IF (sale_data->>'create_delivery')::BOOLEAN = true THEN
        INSERT INTO delivery_orders (sale_id, customer_id, delivery_employee_id, status)
        VALUES (
            v_sale_id, 
            v_customer_id, 
            (sale_data->>'delivery_employee_id')::UUID, 
            'PENDING'
        ) RETURNING id INTO v_delivery_id;

        -- Insert delivery items mirroring sale items
        FOR v_item IN SELECT * FROM jsonb_array_elements(sale_data->'items')
        LOOP
            INSERT INTO delivery_items (delivery_order_id, product_id, quantity)
            VALUES (v_delivery_id, (v_item->>'product_id')::UUID, (v_item->>'quantity')::INT);
        END LOOP;
    END IF;

    RETURN jsonb_build_object('success', true, 'sale_id', v_sale_id, 'invoice_number', v_invoice_number, 'delivery_id', v_delivery_id);
EXCEPTION WHEN OTHERS THEN
    -- In PostgreSQL, raising an exception rolls back the transaction.
    RAISE EXCEPTION 'Transaction failed: %', SQLERRM;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==========================================
-- 3. ROW LEVEL SECURITY (RLS)
-- ==========================================

-- Enable RLS on all tables
ALTER TABLE app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE brands ENABLE ROW LEVEL SECURITY;
ALTER TABLE suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE credit_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Profiles: Users can read all profiles (needed for relations), but only update themselves. Admins can update all.
CREATE POLICY "Profiles read access" ON profiles FOR SELECT USING (true);
CREATE POLICY "Profiles update own" ON profiles FOR UPDATE USING (auth.uid() = id);

-- Base rule for Admins: Complete Access
-- A helper function to check admin status
CREATE OR REPLACE FUNCTION is_admin() RETURNS BOOLEAN AS $$
  SELECT EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'ADMIN');
$$ LANGUAGE sql SECURITY DEFINER;

-- Applies admin full access dynamically to everything via simplified approach:
-- (For real production, you might generate policy statements for each table, but we will create basic ones here)
CREATE POLICY "Full access for admins" ON products FOR ALL USING (is_admin());
CREATE POLICY "Full access for admins" ON customers FOR ALL USING (is_admin());
CREATE POLICY "Full access for admins" ON sales FOR ALL USING (is_admin());
CREATE POLICY "Full access for admins" ON sale_items FOR ALL USING (is_admin());
CREATE POLICY "Full access for admins" ON payments FOR ALL USING (is_admin());

-- Products: Everyone can read products
CREATE POLICY "Anyone can read products" ON products FOR SELECT USING (true);
CREATE POLICY "Anyone can read categories" ON categories FOR SELECT USING (true);
CREATE POLICY "Anyone can read brands" ON brands FOR SELECT USING (true);
CREATE POLICY "Anyone can read stock_batches" ON stock_batches FOR SELECT USING (true);

-- Sellers: Can read customers, create sales, create payments
CREATE POLICY "Sellers can read customers" ON customers FOR SELECT USING (true);
CREATE POLICY "Sellers can create sales" ON sales FOR INSERT WITH CHECK (true);
CREATE POLICY "Sellers can create sale_items" ON sale_items FOR INSERT WITH CHECK (true);
CREATE POLICY "Sellers can create payments" ON payments FOR INSERT WITH CHECK (true);
CREATE POLICY "Sellers can read own sales" ON sales FOR SELECT USING (user_id = auth.uid());

-- Delivery: Can read assigned orders, create payments
CREATE POLICY "Delivery can read assigned orders" ON delivery_orders FOR SELECT USING (delivery_employee_id = auth.uid() OR is_admin());
CREATE POLICY "Delivery can update assigned orders" ON delivery_orders FOR UPDATE USING (delivery_employee_id = auth.uid() OR is_admin());
CREATE POLICY "Admin can delete delivery_orders" ON delivery_orders FOR DELETE USING (is_admin());

-- Manager access (Simplified: Similar to admin but restricted in UI usually, or separate policy)
-- Note: In a real environment, you'll flesh out every CRUD policy per role perfectly.
-- Add RLS policies for delivery_items so delivery drivers can read/update them
CREATE POLICY "Delivery can read assigned delivery_items" ON delivery_items FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM delivery_orders 
        WHERE delivery_orders.id = delivery_items.delivery_order_id 
        AND (delivery_orders.delivery_employee_id = auth.uid() OR is_admin())
    )
);

CREATE POLICY "Delivery can update assigned delivery_items" ON delivery_items FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM delivery_orders 
        WHERE delivery_orders.id = delivery_items.delivery_order_id 
        AND (delivery_orders.delivery_employee_id = auth.uid() OR is_admin())
    )
);

CREATE POLICY "Admin can delete delivery_items" ON delivery_items FOR DELETE USING (
    is_admin()
);
