-- Fix RLS violations on internal tables (like stock_movements) by elevating function privileges.
-- This ensures that when drivers or cashiers perform actions that trigger background updates 
-- (like updating inventory or credit), the database doesn't block them due to RLS.

ALTER FUNCTION public.handle_new_user() SECURITY DEFINER;
ALTER FUNCTION update_customer_credit() SECURITY DEFINER;
ALTER FUNCTION process_delivery_stock() SECURITY DEFINER;
ALTER FUNCTION handle_delivery_item_modification() SECURITY DEFINER;
ALTER FUNCTION process_sale(JSONB) SECURITY DEFINER;
