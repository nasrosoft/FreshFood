-- 1. Add sale_id to delivery_orders
ALTER TABLE delivery_orders ADD COLUMN IF NOT EXISTS sale_id UUID REFERENCES sales(id) ON DELETE SET NULL;

-- 2. Create the function to restock and adjust financials when a delivery item is modified by a driver
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
$$ LANGUAGE plpgsql;

-- 3. Create the trigger (drop if exists first to avoid duplicates)
DROP TRIGGER IF EXISTS trigger_delivery_item_modified ON delivery_items;
CREATE TRIGGER trigger_delivery_item_modified
AFTER UPDATE OR DELETE ON delivery_items
FOR EACH ROW EXECUTE PROCEDURE handle_delivery_item_modification();
