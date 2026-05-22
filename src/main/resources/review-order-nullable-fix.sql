-- Fix review creation from artist profile (standalone reviews)
-- Error fixed: null value in column "order_id" violates not-null constraint

ALTER TABLE app.reviews
ALTER COLUMN order_id DROP NOT NULL;

-- Verify
-- SELECT column_name, is_nullable, data_type
-- FROM information_schema.columns
-- WHERE table_schema='app' AND table_name='reviews' AND column_name='order_id';

