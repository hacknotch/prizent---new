-- ========================================
-- ROLLBACK SCRIPT
-- ========================================
-- WARNING: Only use if you backed up data before migration
-- This script attempts to restore UUID columns
-- ========================================

USE identity_db;

-- ========================================
-- RESTORE FROM BACKUP TABLES
-- ========================================

-- Drop current tables
DROP TABLE IF EXISTS p_login_logout_histories;
DROP TABLE IF EXISTS p_password_recovery_histories;
DROP TABLE IF EXISTS p_users;

-- Restore from backups (if you created them)
CREATE TABLE p_users AS SELECT * FROM p_users_backup;
CREATE TABLE p_login_logout_histories AS SELECT * FROM p_login_logout_histories_backup;
CREATE TABLE p_password_recovery_histories AS SELECT * FROM p_password_recovery_histories_backup;

-- Re-add constraints and indexes
-- (Add your original constraints here)

SELECT 'Rollback completed. Please verify table structures.' AS status;
