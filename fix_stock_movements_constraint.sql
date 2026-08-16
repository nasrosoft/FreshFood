-- Add DELIVERY and DELIVERY_RETURN to stock_movements movement_type check constraint
-- It seems an older version of the trigger or a custom trigger is inserting 'DELIVERY'.
ALTER TABLE stock_movements DROP CONSTRAINT IF EXISTS stock_movements_movement_type_check;
ALTER TABLE stock_movements ADD CONSTRAINT stock_movements_movement_type_check 
CHECK (movement_type IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'LOSS', 'EXPIRED', 'TRANSFER', 'DELIVERY_RETURN', 'DELIVERY'));
