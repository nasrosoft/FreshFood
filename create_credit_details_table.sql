-- ===============================================================================
-- CREATE TABLE customer_credit_details & UPDATE PROCESS_SALE FOR CREDIT AUDIT
-- ===============================================================================

-- 1. Create table customer_credit_details
CREATE TABLE IF NOT EXISTS customer_credit_details (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE,
    sale_id UUID REFERENCES sales(id) ON DELETE SET NULL,
    invoice_number TEXT,
    items_summary TEXT,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    credit_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    transaction_type TEXT CHECK (transaction_type IN ('CREDIT_SALE', 'PAYMENT')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Policies
ALTER TABLE customer_credit_details ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow authenticated read customer_credit_details" ON customer_credit_details;
CREATE POLICY "Allow authenticated read customer_credit_details"
ON customer_credit_details FOR SELECT
TO authenticated USING (true);

DROP POLICY IF EXISTS "Allow authenticated insert customer_credit_details" ON customer_credit_details;
CREATE POLICY "Allow authenticated insert customer_credit_details"
ON customer_credit_details FOR INSERT
TO authenticated WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon read customer_credit_details" ON customer_credit_details;
CREATE POLICY "Allow anon read customer_credit_details"
ON customer_credit_details FOR SELECT
TO anon USING (true);

DROP POLICY IF EXISTS "Allow anon insert customer_credit_details" ON customer_credit_details;
CREATE POLICY "Allow anon insert customer_credit_details"
ON customer_credit_details FOR INSERT
TO anon WITH CHECK (true);

GRANT ALL ON customer_credit_details TO postgres, authenticated, anon, service_role;

-- 2. Update process_sale to record credit details when an admin creates a credit sale
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
    v_items_summary TEXT := '';
    v_prod_name TEXT;
BEGIN
    -- Generate invoice number
    v_invoice_number := 'INV-' || to_char(NOW(), 'YYYYMMDD-HH24MISS');
    
    -- Safely parse UUIDs
    IF sale_data->>'customer_id' IS NOT NULL THEN
        v_customer_id := (sale_data->>'customer_id')::UUID;
    END IF;
    
    IF sale_data->>'user_id' IS NOT NULL THEN
        v_user_id := (sale_data->>'user_id')::UUID;
    END IF;

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

    -- 2. Process Items, FEFO batches, and build items_summary
    FOR v_item IN SELECT * FROM jsonb_array_elements(sale_data->'items')
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_qty_needed := (v_item->>'quantity')::INT;
        v_unit_price := (v_item->>'unit_price')::DECIMAL;
        
        SELECT name, purchase_price INTO v_prod_name, v_cost_price FROM products WHERE id = v_product_id;

        -- Build summary text e.g. "5x Soummam Yogurt, 2x Milka"
        IF v_items_summary <> '' THEN
            v_items_summary := v_items_summary || ', ';
        END IF;
        v_items_summary := v_items_summary || v_qty_needed || 'x ' || COALESCE(v_prod_name, 'Produit');

        FOR v_batch IN 
            SELECT id, quantity FROM stock_batches 
            WHERE product_id = v_product_id AND quantity > 0
            ORDER BY expiration_date ASC
        LOOP
            IF v_qty_needed <= 0 THEN
                EXIT;
            END IF;

            IF v_batch.quantity >= v_qty_needed THEN
                v_qty_to_take := v_qty_needed;
            ELSE
                v_qty_to_take := v_batch.quantity;
            END IF;

            UPDATE stock_batches SET quantity = quantity - v_qty_to_take WHERE id = v_batch.id;
            UPDATE products SET current_stock = current_stock - v_qty_to_take WHERE id = v_product_id;

            INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
            VALUES (v_product_id, v_batch.id, -v_qty_to_take, 'SALE', v_sale_id, v_user_id);

            INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
            VALUES (v_sale_id, v_product_id, v_batch.id, v_qty_to_take, v_unit_price, v_cost_price, v_qty_to_take * v_unit_price);

            v_qty_needed := v_qty_needed - v_qty_to_take;
        END LOOP;

        IF v_qty_needed > 0 THEN
            UPDATE products SET current_stock = current_stock - v_qty_needed WHERE id = v_product_id;
            INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
            VALUES (v_product_id, NULL, -v_qty_needed, 'SALE', v_sale_id, v_user_id);
            INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
            VALUES (v_sale_id, v_product_id, NULL, v_qty_needed, v_unit_price, v_cost_price, v_qty_needed * v_unit_price);
        END IF;
    END LOOP;

    -- 3. Handle Credit & Payment records
    IF (sale_data->>'credit_amount')::DECIMAL > 0 AND v_customer_id IS NOT NULL THEN
        -- Insert into credit transactions ledger
        INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
        VALUES (v_customer_id, (sale_data->>'credit_amount')::DECIMAL, 'DEBT', v_sale_id, v_user_id);

        -- Insert into customer_credit_details with product items summary
        INSERT INTO customer_credit_details (
            customer_id, sale_id, invoice_number, items_summary, total_amount, credit_amount, transaction_type
        ) VALUES (
            v_customer_id,
            v_sale_id,
            v_invoice_number,
            v_items_summary,
            (sale_data->>'total_amount')::DECIMAL,
            (sale_data->>'credit_amount')::DECIMAL,
            'CREDIT_SALE'
        );
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

        FOR v_item IN SELECT * FROM jsonb_array_elements(sale_data->'items')
        LOOP
            INSERT INTO delivery_items (delivery_order_id, product_id, quantity)
            VALUES (v_delivery_id, (v_item->>'product_id')::UUID, (v_item->>'quantity')::INT);
        END LOOP;
    END IF;

    RETURN jsonb_build_object('success', true, 'sale_id', v_sale_id, 'invoice_number', v_invoice_number, 'delivery_id', v_delivery_id);
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Transaction failed: %', SQLERRM;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Backfill existing credit sales into customer_credit_details so current clients show their past credit order details
INSERT INTO customer_credit_details (customer_id, sale_id, invoice_number, items_summary, total_amount, credit_amount, transaction_type, created_at)
SELECT 
    s.customer_id,
    s.id,
    s.invoice_number,
    (
        SELECT string_agg(si.quantity || 'x ' || p.name, ', ')
        FROM sale_items si
        JOIN products p ON p.id = si.product_id
        WHERE si.sale_id = s.id
    ) AS items_summary,
    s.total_amount,
    s.credit_amount,
    'CREDIT_SALE',
    s.created_at
FROM sales s
WHERE s.credit_amount > 0 AND s.customer_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM customer_credit_details ccd WHERE ccd.sale_id = s.id);
