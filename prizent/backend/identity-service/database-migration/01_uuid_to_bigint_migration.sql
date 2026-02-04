-- ========================================
-- UUID TO BIGINT MIGRATION - IDENTITY_DB
-- ========================================
-- This script migrates all UUID/BINARY(16) columns to BIGINT
-- CAUTION: This will DELETE all existing data in affected tables
-- Backup your database before running this script!
-- ========================================

USE identity_db;

-- ========================================
-- STEP 1: DROP FOREIGN KEY CONSTRAINTS
-- ========================================

-- Drop FK from p_login_logout_histories if exists
ALTER TABLE p_login_logout_histories 
DROP FOREIGN KEY IF EXISTS fk_login_logout_user;

-- Drop FK from p_password_recovery_histories if exists
ALTER TABLE p_password_recovery_histories 
DROP FOREIGN KEY IF EXISTS fk_password_recovery_user;

-- ========================================
-- STEP 2: BACKUP TABLE STRUCTURES (OPTIONAL)
-- ========================================
-- Uncomment if you want to backup data
-- CREATE TABLE p_users_backup AS SELECT * FROM p_users;
-- CREATE TABLE p_login_logout_histories_backup AS SELECT * FROM p_login_logout_histories;
-- CREATE TABLE p_password_recovery_histories_backup AS SELECT * FROM p_password_recovery_histories;

-- ========================================
-- STEP 3: MIGRATE p_users TABLE
-- ========================================

-- Drop existing data (no critical data migration needed for development)
TRUNCATE TABLE p_login_logout_histories;
TRUNCATE TABLE p_password_recovery_histories;
TRUNCATE TABLE p_users;

-- Drop old UUID id column
ALTER TABLE p_users DROP COLUMN id;

-- Add new BIGINT id column with AUTO_INCREMENT
ALTER TABLE p_users 
ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Verify structure
SHOW COLUMNS FROM p_users LIKE 'id';

-- ========================================
-- STEP 4: MIGRATE p_login_logout_histories TABLE
-- ========================================

-- Drop old UUID columns
ALTER TABLE p_login_logout_histories DROP COLUMN id;
ALTER TABLE p_login_logout_histories DROP COLUMN user_id;

-- Add new BIGINT columns
ALTER TABLE p_login_logout_histories 
ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE p_login_logout_histories 
ADD COLUMN user_id BIGINT NOT NULL AFTER client_id;

-- Verify structure
SHOW COLUMNS FROM p_login_logout_histories;

-- ========================================
-- STEP 5: MIGRATE p_password_recovery_histories TABLE
-- ========================================

-- Drop old UUID user_id column
ALTER TABLE p_password_recovery_histories DROP COLUMN user_id;

-- Add new BIGINT user_id column
ALTER TABLE p_password_recovery_histories 
ADD COLUMN user_id BIGINT NOT NULL AFTER client_id;

-- Verify structure
SHOW COLUMNS FROM p_password_recovery_histories;

-- ========================================
-- STEP 6: ADD FOREIGN KEY CONSTRAINTS
-- ========================================

-- Add FK for p_login_logout_histories -> p_users
ALTER TABLE p_login_logout_histories
ADD CONSTRAINT fk_login_logout_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Add FK for p_password_recovery_histories -> p_users
ALTER TABLE p_password_recovery_histories
ADD CONSTRAINT fk_password_recovery_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- ========================================
-- STEP 7: ADD INDEXES FOR PERFORMANCE
-- ========================================

-- Index on user_id for login_logout_histories
CREATE INDEX idx_login_logout_user_id ON p_login_logout_histories(user_id);
CREATE INDEX idx_login_logout_client_user ON p_login_logout_histories(client_id, user_id);

-- Index on user_id for password_recovery_histories
CREATE INDEX idx_password_recovery_user_id ON p_password_recovery_histories(user_id);
CREATE INDEX idx_password_recovery_client_user ON p_password_recovery_histories(client_id, user_id);

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Check all columns in identity_db for UUID/BINARY types
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND (
    DATA_TYPE = 'binary' 
    OR COLUMN_TYPE LIKE '%BINARY(16)%'
    OR COLUMN_TYPE LIKE '%CHAR(36)%'
    OR DATA_TYPE = 'uuid'
);

-- Should return ZERO rows if migration is successful

-- Check all BIGINT columns (should include our migrated columns)
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND DATA_TYPE = 'bigint'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Check foreign key relationships
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'identity_db'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ========================================
-- SUMMARY
-- ========================================
-- Migration completed for:
-- 1. p_users: id BINARY(16) -> BIGINT AUTO_INCREMENT
-- 2. p_login_logout_histories: id BINARY(16) -> BIGINT, user_id BINARY(16) -> BIGINT
-- 3. p_password_recovery_histories: user_id BINARY(16) -> BIGINT
-- 4. p_clients: Already using INT (no changes)
-- ========================================
