-- ========================================
-- QUICK MIGRATION COMMAND REFERENCE
-- ========================================

-- BACKUP (Run first!)
mysqldump -u root -p identity_db > backup_$(date +%Y%m%d_%H%M%S).sql

-- DEVELOPMENT: Run migration (deletes data)
mysql -u root -p identity_db < 01_uuid_to_bigint_migration.sql

-- PRODUCTION: Run migration (preserves data)
mysql -u root -p identity_db < 03_production_migration_with_data.sql

-- VERIFY: Check for violations
mysql -u root -p identity_db < 02_verification_queries.sql

-- FINAL CHECK: Should return "0"
mysql -u root -p identity_db -e "
SELECT COUNT(*) AS uuid_violations
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND (DATA_TYPE = 'binary' OR COLUMN_TYPE LIKE '%BINARY(16)%');"

-- ========================================
-- MIGRATION SUMMARY
-- ========================================
-- Tables Migrated:
--   p_users: id BINARY(16) -> BIGINT AUTO_INCREMENT
--   p_login_logout_histories: id, user_id BINARY(16) -> BIGINT
--   p_password_recovery_histories: user_id BINARY(16) -> BIGINT
--
-- Foreign Keys Updated:
--   fk_login_logout_user
--   fk_password_recovery_user
--
-- Indexes Added:
--   idx_login_logout_user_id
--   idx_login_logout_client_user
--   idx_password_recovery_user_id
--   idx_password_recovery_client_user
-- ========================================
