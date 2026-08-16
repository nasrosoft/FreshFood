-- Add RLS policies for delivery_items so delivery drivers can read/update them
DROP POLICY IF EXISTS "Delivery can read assigned delivery_items" ON delivery_items;
CREATE POLICY "Delivery can read assigned delivery_items" ON delivery_items FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM delivery_orders 
        WHERE delivery_orders.id = delivery_items.delivery_order_id 
        AND (delivery_orders.delivery_employee_id = auth.uid() OR is_admin())
    )
);

DROP POLICY IF EXISTS "Delivery can update assigned delivery_items" ON delivery_items;
CREATE POLICY "Delivery can update assigned delivery_items" ON delivery_items FOR UPDATE USING (
    EXISTS (
        SELECT 1 FROM delivery_orders 
        WHERE delivery_orders.id = delivery_items.delivery_order_id 
        AND (delivery_orders.delivery_employee_id = auth.uid() OR is_admin())
    )
);

DROP POLICY IF EXISTS "Delivery can delete assigned delivery_items" ON delivery_items;
DROP POLICY IF EXISTS "Admin can delete delivery_items" ON delivery_items;
CREATE POLICY "Admin can delete delivery_items" ON delivery_items FOR DELETE USING (
    is_admin()
);
