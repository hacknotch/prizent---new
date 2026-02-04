-- Migration script for MySQL 8.0
USE identity_db;

-- Step 1: Truncate dependent tables
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE p_login_logout_histories;
SET FOREIGN_KEY_CHECKS=1;

-- Step 2: Truncate users table
TRUNCATE TABLE p_users;

-- Step 3: Drop and recreate p_users with BIGINT
ALTER TABLE p_users DROP PRIMARY KEY;
ALTER TABLE p_users DROP COLUMN id;
ALTER TABLE p_users ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Step 4: Update p_login_logout_histories
ALTER TABLE p_login_logout_histories DROP PRIMARY KEY;
ALTER TABLE p_login_logout_histories DROP COLUMN id;
ALTER TABLE p_login_logout_histories DROP COLUMN user_id;

ALTER TABLE p_login_logout_histories ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE p_login_logout_histories ADD COLUMN user_id BIGINT NOT NULL AFTER client_id;

-- Step 5: Create password recovery histories table
CREATE TABLE IF NOT EXISTS p_password_recovery_histories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    user_id BIGINT NOT NULL,
    old_password VARCHAR(255) NOT NULL,
    new_password VARCHAR(255) NOT NULL,
    changed_time_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_client_user (client_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 6: Add foreign keys
ALTER TABLE p_login_logout_histories
ADD CONSTRAINT fk_login_logout_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE;

ALTER TABLE p_password_recovery_histories
ADD CONSTRAINT fk_password_recovery_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) 
ON DELETE CASCADE;

-- Step 7: Add indexes
CREATE INDEX idx_login_logout_user_id ON p_login_logout_histories(user_id);
CREATE INDEX idx_login_logout_client_user ON p_login_logout_histories(client_id, user_id);

SELECT 'Migration completed successfully!' AS status;
