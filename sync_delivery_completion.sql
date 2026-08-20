-- ===============================================================================
-- DRAFT ORDER & LIVRÉE COMMIT WORKFLOW
-- ===============================================================================

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
    v_is_delivery BOOLEAN := FALSE;
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

    IF (sale_data->>'create_delivery')::BOOLEAN = TRUE THEN
        v_is_delivery := TRUE;
    END IF;

    -- 1. Insert Sale record (PENDING for delivery draft, COMPLETED for direct counter sale)
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
        CASE WHEN v_is_delivery THEN 'PENDING' ELSE 'COMPLETED' END
    ) RETURNING id INTO v_sale_id;

    -- 2. Process Items
    FOR v_item IN SELECT * FROM jsonb_array_elements(sale_data->'items')
    LOOP
        v_product_id := (v_item->>'product_id')::UUID;
        v_qty_needed := (v_item->>'quantity')::INT;
        v_unit_price := (v_item->>'unit_price')::DECIMAL;
        
        SELECT name, purchase_price INTO v_prod_name, v_cost_price FROM products WHERE id = v_product_id;

        IF v_items_summary <> '' THEN
            v_items_summary := v_items_summary || ', ';
        END IF;
        v_items_summary := v_items_summary || v_qty_needed || 'x ' || COALESCE(v_prod_name, 'Produit');

        IF v_is_delivery THEN
            -- DRAFT / DELIVERY ORDER: Record items without deducting stock or batches
            INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
            VALUES (v_sale_id, v_product_id, NULL, v_qty_needed, v_unit_price, v_cost_price, v_qty_needed * v_unit_price);
        ELSE
            -- DIRECT POS SALE: Immediately deduct from FEFO batches and stock
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
        END IF;
    END LOOP;

    -- 3. Handle Credit & Payment records ONLY for direct counter sales
    IF NOT v_is_delivery THEN
        IF (sale_data->>'credit_amount')::DECIMAL > 0 AND v_customer_id IS NOT NULL THEN
            INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
            VALUES (v_customer_id, (sale_data->>'credit_amount')::DECIMAL, 'DEBT', v_sale_id, v_user_id);

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
    END IF;

    -- 4. Handle Delivery Creation
    IF v_is_delivery THEN
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


-- Update sync_delivery_completion function (LIVRÉE Commit Point)
CREATE OR REPLACE FUNCTION sync_delivery_completion(
    p_delivery_id UUID,
    p_modified_quantities JSONB,
    p_final_payment_method TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_sale_id UUID;
    v_customer_id UUID;
    v_user_id UUID;
    v_invoice_number TEXT;
    
    v_del_item RECORD;
    v_batch RECORD;
    
    v_final_qty INT;
    v_qty_needed INT;
    v_qty_to_take INT;
    v_unit_price DECIMAL(12,2);
    v_cost_price DECIMAL(12,2);
    
    v_new_total_amount DECIMAL(12,2) := 0.00;
    v_items_summary TEXT := '';
    v_has_legacy_batches BOOLEAN := FALSE;
BEGIN
    -- 1. Fetch related delivery and sale data
    SELECT sale_id, customer_id, delivery_employee_id 
    INTO v_sale_id, v_customer_id, v_user_id 
    FROM delivery_orders WHERE id = p_delivery_id;
    
    IF v_sale_id IS NULL THEN
        RAISE EXCEPTION 'Delivery order has no associated sale.';
    END IF;

    SELECT invoice_number INTO v_invoice_number
    FROM sales WHERE id = v_sale_id;

    -- Check if sale items were already allocated under the legacy system
    SELECT EXISTS (SELECT 1 FROM sale_items WHERE sale_id = v_sale_id AND batch_id IS NOT NULL)
    INTO v_has_legacy_batches;

    IF v_has_legacy_batches THEN
        -- LEGACY CLEANUP: If stock was previously deducted under old system, return it first to re-sync cleanly
        FOR v_del_item IN SELECT * FROM sale_items WHERE sale_id = v_sale_id
        LOOP
            IF v_del_item.batch_id IS NOT NULL THEN
                UPDATE stock_batches SET quantity = quantity + v_del_item.quantity WHERE id = v_del_item.batch_id;
            END IF;
            UPDATE products SET current_stock = current_stock + v_del_item.quantity WHERE id = v_del_item.product_id;
        END LOOP;
        DELETE FROM stock_movements WHERE reference_id = v_sale_id;
    END IF;

    -- Clear temporary draft sale items to rebuild cleanly with final delivered quantities
    DELETE FROM sale_items WHERE sale_id = v_sale_id;

    -- 2. Process final delivered quantities and deduct stock (Single commit point)
    FOR v_del_item IN SELECT * FROM delivery_items WHERE delivery_order_id = p_delivery_id
    LOOP
        -- Resolve final quantity (support lookup by delivery_item.id or product_id)
        v_final_qty := COALESCE(
            (p_modified_quantities->>v_del_item.id::TEXT)::INT,
            (p_modified_quantities->>v_del_item.product_id::TEXT)::INT,
            v_del_item.quantity
        );

        IF v_final_qty > 0 THEN
            SELECT selling_price, purchase_price INTO v_unit_price, v_cost_price 
            FROM products WHERE id = v_del_item.product_id;

            v_qty_needed := v_final_qty;

            -- Deduct from stock batches using FEFO
            FOR v_batch IN 
                SELECT id, quantity FROM stock_batches 
                WHERE product_id = v_del_item.product_id AND quantity > 0
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
                UPDATE products SET current_stock = current_stock - v_qty_to_take WHERE id = v_del_item.product_id;

                INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
                VALUES (v_del_item.product_id, v_batch.id, -v_qty_to_take, 'DELIVERY', v_sale_id, v_user_id);

                INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
                VALUES (v_sale_id, v_del_item.product_id, v_batch.id, v_qty_to_take, v_unit_price, v_cost_price, v_qty_to_take * v_unit_price);

                v_qty_needed := v_qty_needed - v_qty_to_take;
            END LOOP;

            -- If remaining quantity exceeds batch stock, deduct from global product stock
            IF v_qty_needed > 0 THEN
                UPDATE products SET current_stock = current_stock - v_qty_needed WHERE id = v_del_item.product_id;
                
                INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
                VALUES (v_del_item.product_id, NULL, -v_qty_needed, 'DELIVERY', v_sale_id, v_user_id);

                INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, cost_price, subtotal)
                VALUES (v_sale_id, v_del_item.product_id, NULL, v_qty_needed, v_unit_price, v_cost_price, v_qty_needed * v_unit_price);
            END IF;

            -- Update delivery item to final delivered quantity
            UPDATE delivery_items SET quantity = v_final_qty WHERE id = v_del_item.id;
        ELSE
            -- 0 quantity: remove item from delivery manifest
            DELETE FROM delivery_items WHERE id = v_del_item.id;
        END IF;
    END LOOP;

    -- 3. Calculate true final total and build item summary
    SELECT COALESCE(SUM(subtotal), 0.00) INTO v_new_total_amount FROM sale_items WHERE sale_id = v_sale_id;
    
    SELECT string_agg(si.quantity || 'x ' || p.name, ', ')
    INTO v_items_summary
    FROM sale_items si
    JOIN products p ON p.id = si.product_id
    WHERE si.sale_id = v_sale_id;
    
    v_items_summary := COALESCE(v_items_summary, '');

    -- 4. Financial & Customer Credit Reconciliation (Source of Truth)
    IF p_final_payment_method ILIKE 'CREDIT' THEN
        -- Finalize sale as CREDIT
        UPDATE sales 
        SET payment_method = 'CREDIT', 
            total_amount = v_new_total_amount, 
            credit_amount = v_new_total_amount, 
            paid_amount = 0.00,
            status = 'COMPLETED'
        WHERE id = v_sale_id;
        
        DELETE FROM payments WHERE reference_id = v_sale_id;
        
        -- Insert or update single DEBT transaction for exact delivered amount
        IF v_customer_id IS NOT NULL THEN
            IF EXISTS (SELECT 1 FROM credit_transactions WHERE reference_id = v_sale_id AND transaction_type = 'DEBT') THEN
                UPDATE credit_transactions 
                SET amount = v_new_total_amount 
                WHERE reference_id = v_sale_id AND transaction_type = 'DEBT';
            ELSE
                INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
                VALUES (v_customer_id, v_new_total_amount, 'DEBT', v_sale_id, v_user_id);
            END IF;

            -- Insert or update customer_credit_details audit log
            IF EXISTS (SELECT 1 FROM customer_credit_details WHERE sale_id = v_sale_id) THEN
                UPDATE customer_credit_details
                SET total_amount = v_new_total_amount, 
                    credit_amount = v_new_total_amount, 
                    items_summary = v_items_summary,
                    transaction_type = 'CREDIT_SALE'
                WHERE sale_id = v_sale_id;
            ELSE
                INSERT INTO customer_credit_details (customer_id, sale_id, invoice_number, items_summary, total_amount, credit_amount, transaction_type)
                VALUES (v_customer_id, v_sale_id, v_invoice_number, v_items_summary, v_new_total_amount, v_new_total_amount, 'CREDIT_SALE');
            END IF;
        END IF;

    ELSE
        -- Finalize sale as CASH
        UPDATE sales 
        SET payment_method = 'CASH',
            total_amount = v_new_total_amount, 
            paid_amount = v_new_total_amount, 
            credit_amount = 0.00,
            status = 'COMPLETED'
        WHERE id = v_sale_id;
        
        -- Remove any temporary credit transaction so customer debt is never inflated
        DELETE FROM credit_transactions WHERE reference_id = v_sale_id;
        DELETE FROM customer_credit_details WHERE sale_id = v_sale_id;
        
        IF EXISTS (SELECT 1 FROM payments WHERE reference_id = v_sale_id) THEN
            UPDATE payments SET amount = v_new_total_amount, payment_method = 'CASH' WHERE reference_id = v_sale_id;
        ELSIF v_customer_id IS NOT NULL THEN
            INSERT INTO payments (customer_id, amount, payment_method, reference_id, user_id)
            VALUES (v_customer_id, v_new_total_amount, 'CASH', v_sale_id, v_user_id);
        END IF;
    END IF;

    -- 5. Mark Delivery as DELIVERED
    UPDATE delivery_orders SET status = 'DELIVERED', updated_at = NOW() WHERE id = p_delivery_id;

    RETURN jsonb_build_object(
        'success', true, 
        'delivery_id', p_delivery_id, 
        'sale_id', v_sale_id, 
        'final_amount', v_new_total_amount,
        'payment_method', p_final_payment_method,
        'status', 'DELIVERED'
    );
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Sync delivery completion failed: %', SQLERRM;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
