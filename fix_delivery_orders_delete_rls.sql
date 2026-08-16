-- Add DELETE policy for admins on delivery_orders
CREATE POLICY "Admin can delete delivery_orders" ON delivery_orders FOR DELETE USING (is_admin());
