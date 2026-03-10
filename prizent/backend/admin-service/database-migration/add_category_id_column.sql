-- Add category_id column to p_marketplace_costs table
-- This stores the category filter for a cost slab as a proper FK instead of embedding it in cost_product_range

ALTER TABLE p_marketplace_costs
    ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT 'Category ID filter for this cost slab (NULL = applies to all categories)';

CREATE INDEX idx_marketplace_costs_category_id ON p_marketplace_costs(category_id);

SELECT 'Migration completed: category_id column added to p_marketplace_costs' AS Status;
