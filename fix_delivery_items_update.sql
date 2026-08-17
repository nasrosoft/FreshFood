-- =========================================================
-- FIX: Delivery Item Modification & Sale Items Constraint
-- =========================================================

-- 1. Relax the sale_items check constraint to allow quantity >= 0
ALTER TABLE sale_items DROP CONSTRAINT IF EXISTS sale_items_quantity_check;
ALTER TABLE sale_items ADD CONSTRAINT sale_items_quantity_check CHECK (quantity >= 0);

-- 2. Update handle_delivery_item_modification function
CREATE OR REPLACE FUNCTION handle_delivery_item_modification()
RETURNS TRIGGER AS $$
DECLARE
    v_diff INT;
    v_remaining_diff INT;
    v_take_qty INT;
    v_sale_id UUID;
    v_customer_id UUID;
    v_user_id UUID;
    v_sale_item RECORD;
    v_unit_price DECIMAL(12,2);
    v_refund_amount DECIMAL(12,2) := 0;
    v_item_refund DECIMAL(12,2);
BEGIN
    -- Only proceed if it's an UPDATE (quantity reduced) or DELETE
    IF TG_OP = 'DELETE' THEN
        v_diff := OLD.quantity;
    ELSIF TG_OP = 'UPDATE' AND NEW.quantity < OLD.quantity THEN
        v_diff := OLD.quantity - NEW.quantity;
    ELSE
        RETURN COALESCE(NEW, OLD);
    END IF;

    -- Get the delivery order to find sale_id, customer_id, and delivery employee
    SELECT sale_id, customer_id, delivery_employee_id INTO v_sale_id, v_customer_id, v_user_id
    FROM delivery_orders WHERE id = OLD.delivery_order_id;

    -- 1. Restock physical item
    UPDATE products SET current_stock = current_stock + v_diff WHERE id = OLD.product_id;
    INSERT INTO stock_movements (product_id, batch_id, quantity, movement_type, reference_id, user_id)
    VALUES (OLD.product_id, NULL, v_diff, 'DELIVERY_RETURN', OLD.delivery_order_id, v_user_id);

    -- 2. If linked to a sale, adjust financial records safely across all matched sale items
    IF v_sale_id IS NOT NULL THEN
        v_remaining_diff := v_diff;

        -- Loop through sale_items for this product to deduct quantities without going negative on any single row
        FOR v_sale_item IN 
            SELECT * FROM sale_items 
            WHERE sale_id = v_sale_id AND product_id = OLD.product_id AND quantity > 0
            ORDER BY quantity DESC
        LOOP
            IF v_remaining_diff <= 0 THEN
                EXIT;
            END IF;

            v_take_qty := LEAST(v_sale_item.quantity, v_remaining_diff);
            v_unit_price := v_sale_item.unit_price;
            v_item_refund := v_take_qty * v_unit_price;
            v_refund_amount := v_refund_amount + v_item_refund;

            -- Update the sale_item quantity & subtotal
            UPDATE sale_items 
            SET quantity = quantity - v_take_qty, 
                subtotal = subtotal - v_item_refund
            WHERE id = v_sale_item.id;

            v_remaining_diff := v_remaining_diff - v_take_qty;
        END LOOP;

        -- Update sales total amount
        IF v_refund_amount > 0 THEN
            UPDATE sales 
            SET total_amount = total_amount - v_refund_amount
            WHERE id = v_sale_id;
            
            -- Insert a PAYMENT credit transaction to reduce customer's debt for returned items
            IF v_customer_id IS NOT NULL THEN
                INSERT INTO credit_transactions (customer_id, amount, transaction_type, reference_id, user_id)
                VALUES (v_customer_id, v_refund_amount, 'PAYMENT', v_sale_id, v_user_id);
            END IF;
        END IF;
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Re-create the trigger
DROP TRIGGER IF EXISTS trigger_delivery_item_modified ON delivery_items;
CREATE TRIGGER trigger_delivery_item_modified
AFTER UPDATE OR DELETE ON delivery_items
FOR EACH ROW EXECUTE PROCEDURE handle_delivery_item_modification();
