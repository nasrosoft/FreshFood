-- Relax the sale_items quantity check constraint to allow 0
-- This is necessary so that if a delivery is fully cancelled or deleted, 
-- the corresponding sale_item can be reduced to 0 to balance the financials without failing the constraint.
ALTER TABLE sale_items DROP CONSTRAINT IF EXISTS sale_items_quantity_check;
ALTER TABLE sale_items ADD CONSTRAINT sale_items_quantity_check CHECK (quantity >= 0);
