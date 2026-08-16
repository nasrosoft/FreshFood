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

CREATE POLICY "Delivery can delete assigned delivery_items" ON delivery_items FOR DELETE USING (
    EXISTS (
        SELECT 1 FROM delivery_orders 
        WHERE delivery_orders.id = delivery_items.delivery_order_id 
        AND (delivery_orders.delivery_employee_id = auth.uid() OR is_admin())
    )
);
