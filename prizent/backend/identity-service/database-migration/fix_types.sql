-- Fix data type mismatches and create missing table
USE identity_db;

-- Fix user_id in login_logout_histories to match p_users.id (BIGINT)
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE p_login_logout_histories;
ALTER TABLE p_login_logout_histories MODIFY COLUMN user_id BIGINT NOT NULL;
SET FOREIGN_KEY_CHECKS=1;

-- Create password recovery table if not exists
CREATE TABLE IF NOT EXISTS p_password_recovery_histories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    user_id BIGINT NOT NULL,
    old_password VARCHAR(255) NOT NULL,
    new_password VARCHAR(255) NOT NULL,
    changed_time_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_recovery_user FOREIGN KEY (user_id) REFERENCES p_users(id) ON DELETE CASCADE,
    INDEX idx_password_recovery_user_id (user_id),
    INDEX idx_password_recovery_client_user (client_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add foreign key for login_logout_histories if not exists
ALTER TABLE p_login_logout_histories DROP FOREIGN KEY IF EXISTS fk_login_logout_user;
ALTER TABLE p_login_logout_histories
ADD CONSTRAINT fk_login_logout_user 
FOREIGN KEY (user_id) REFERENCES p_users(id) ON DELETE CASCADE;

SELECT 'Database migration completed! All ID types are correct.' AS status;
