-- Update process_sale to handle delivery creation
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
