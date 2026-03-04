-- Add product_number and style_code columns to p_products table
ALTER TABLE p_products
    ADD COLUMN product_number VARCHAR(100) NULL AFTER name,
    ADD COLUMN style_code     VARCHAR(100) NULL AFTER product_number;
