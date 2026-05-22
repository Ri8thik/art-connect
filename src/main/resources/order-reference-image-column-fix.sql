-- Fix order reference image URL length issue
-- Error: value too long for type character varying(255)

ALTER TABLE app.order_reference_images
ALTER COLUMN image_url SET DATA TYPE TEXT;

-- Verify
-- SELECT column_name, data_type, character_maximum_length
-- FROM information_schema.columns
-- WHERE table_schema='app' AND table_name='order_reference_images' AND column_name='image_url';

