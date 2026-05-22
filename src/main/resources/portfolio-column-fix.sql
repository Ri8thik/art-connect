-- Migration script to fix portfolio_items column sizes
-- This fixes: "ERROR: value too long for type character varying(255)"

-- Drop the constraint on image_url if it exists and recreate with TEXT type
ALTER TABLE app.portfolio_items
DROP CONSTRAINT IF EXISTS portfolio_items_image_url_check CASCADE;

ALTER TABLE app.portfolio_items
ALTER COLUMN image_url SET DATA TYPE TEXT;

-- Update title column to varchar(500)
ALTER TABLE app.portfolio_items
ALTER COLUMN title SET DATA TYPE varchar(500);

-- Update category column to varchar(500) instead of default 255
ALTER TABLE app.portfolio_items
ALTER COLUMN category SET DATA TYPE varchar(500);

-- Verify the changes
-- SELECT column_name, data_type, character_maximum_length
-- FROM information_schema.columns
-- WHERE table_name = 'portfolio_items' AND table_schema = 'app'
-- ORDER BY ordinal_position;

