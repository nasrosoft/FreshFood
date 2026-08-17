-- Update process_delivery_stock to remove the double-deduction bug
-- The stock is now deducted immediately when the sale/delivery is created in process_sale.
-- This trigger should only restock items if the delivery is cancelled.
CREATE OR REPLACE FUNCTION process_delivery_stock()
RETURNS TRIGGER AS $$
DECLARE
    v_item RECORD;
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
