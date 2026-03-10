-- Migration: Fix marketplace schema to support extended cost categories
-- Run this against admin_db

-- 1. Add missing acc_no column to p_marketplaces (was in entity but not in original migration)
ALTER TABLE p_marketplaces
  ADD COLUMN IF NOT EXISTS acc_no VARCHAR(100) DEFAULT NULL AFTER name;

-- 2. Expand cost_category column from VARCHAR(20) to VARCHAR(50)
ALTER TABLE p_marketplace_costs
  MODIFY COLUMN cost_category VARCHAR(50) NOT NULL;

-- 3. Drop the restrictive CHECK constraint on cost_category (MySQL auto-names them)
--    Try all likely auto-generated names — only the matching one will succeed
ALTER TABLE p_marketplace_costs DROP CHECK p_marketplace_costs_chk_1;
