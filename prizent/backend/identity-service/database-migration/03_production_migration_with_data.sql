-- ========================================
-- PRODUCTION DATA MIGRATION SCRIPT
-- ========================================
-- Use this script if you have existing data that needs to be preserved
-- This creates a deterministic mapping from UUID to BIGINT
-- ========================================

USE identity_db;

-- ========================================
-- PHASE 1: ADD NEW COLUMNS
-- ========================================

-- Add new_id column to p_users
ALTER TABLE p_users 
ADD COLUMN new_id BIGINT NOT NULL FIRST;

-- Add new_id column to p_login_logout_histories
ALTER TABLE p_login_logout_histories 
ADD COLUMN new_id BIGINT NOT NULL FIRST;

-- Add new_user_id column to p_login_logout_histories
ALTER TABLE p_login_logout_histories 
ADD COLUMN new_user_id BIGINT NOT NULL AFTER client_id;

-- Add new_user_id column to p_password_recovery_histories
ALTER TABLE p_password_recovery_histories 
ADD COLUMN new_user_id BIGINT NOT NULL AFTER client_id;

-- ========================================
-- PHASE 2: DATA MIGRATION WITH MAPPING
-- ========================================

-- Create mapping table for UUID -> BIGINT conversion
CREATE TABLE IF NOT EXISTS uuid_to_bigint_mapping (
    old_uuid BINARY(16) PRIMARY KEY,
    new_bigint BIGINT NOT NULL UNIQUE,
    table_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Generate sequential IDs for users
SET @row_number = 0;
UPDATE p_users
SET new_id = (@row_number := @row_number + 1);

-- Store mapping for users
INSERT INTO uuid_to_bigint_mapping (old_uuid, new_bigint, table_name)
SELECT id, new_id, 'p_users' FROM p_users;

-- Generate sequential IDs for login_logout_histories
SET @row_number = 0;
UPDATE p_login_logout_histories
SET new_id = (@row_number := @row_number + 1);

-- Map user_id in login_logout_histories
UPDATE p_login_logout_histories llh
JOIN uuid_to_bigint_mapping m ON llh.user_id = m.old_uuid AND m.table_name = 'p_users'
SET llh.new_user_id = m.new_bigint;

-- Map user_id in password_recovery_histories
UPDATE p_password_recovery_histories prh
JOIN uuid_to_bigint_mapping m ON prh.user_id = m.old_uuid AND m.table_name = 'p_users'
SET prh.new_user_id = m.new_bigint;

-- ========================================
-- PHASE 3: DROP OLD COLUMNS
-- ========================================

-- Drop foreign key constraints first
ALTER TABLE p_login_logout_histories 
DROP FOREIGN KEY IF EXISTS fk_login_logout_user;

ALTER TABLE p_password_recovery_histories 
DROP FOREIGN KEY IF EXISTS fk_password_recovery_user;

-- Drop old UUID columns
ALTER TABLE p_users DROP COLUMN id;
ALTER TABLE p_login_logout_histories DROP COLUMN id;
ALTER TABLE p_login_logout_histories DROP COLUMN user_id;
ALTER TABLE p_password_recovery_histories DROP COLUMN user_id;

-- ========================================
-- PHASE 4: RENAME NEW COLUMNS
-- ========================================

ALTER TABLE p_users CHANGE COLUMN new_id id BIGINT AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE p_login_logout_histories CHANGE COLUMN new_id id BIGINT AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE p_login_logout_histories CHANGE COLUMN new_user_id user_id BIGINT NOT NULL;
ALTER TABLE p_password_recovery_histories CHANGE COLUMN new_user_id user_id BIGINT NOT NULL;

-- ========================================
-- PHASE 5: RESTORE FOREIGN KEYS
-- ========================================

ALTER TABLE p_login_logout_histories
ADD CONSTRAINT fk_login_logout_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE p_password_recovery_histories
ADD CONSTRAINT fk_password_recovery_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- ========================================
-- PHASE 6: ADD INDEXES
-- ========================================

CREATE INDEX idx_login_logout_user_id ON p_login_logout_histories(user_id);
CREATE INDEX idx_login_logout_client_user ON p_login_logout_histories(client_id, user_id);
CREATE INDEX idx_password_recovery_user_id ON p_password_recovery_histories(user_id);
CREATE INDEX idx_password_recovery_client_user ON p_password_recovery_histories(client_id, user_id);

-- ========================================
-- PHASE 7: CLEANUP
-- ========================================

-- Keep mapping table for reference (optional: drop after verification)
-- DROP TABLE uuid_to_bigint_mapping;

-- ========================================
-- VERIFICATION
-- ========================================

SELECT 'Migration completed. Mapping table preserved for reference.' AS status;
SELECT COUNT(*) AS mapped_records FROM uuid_to_bigint_mapping;
