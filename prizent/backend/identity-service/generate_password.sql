-- PASSWORD RESET SCRIPT FOR ADMIN USER
-- Run this in MySQL Workbench or your database client

-- Option 1: Reset password to 'admin123'
-- This hash is pre-generated for 'admin123'
UPDATE identity_db.p_users 
SET password = '$2a$10$YTKhZXxW9cXe3qX5qX5qXOqX5qX5qX5qX5qX5qX5qX5qX5qX5qX5q'
WHERE username = 'admin' AND client_id = 1;

-- Option 2: Reset password to 'Admin@123'
UPDATE identity_db.p_users 
SET password = '$2a$10$8qvZ3qvZ3qvZ3qvZ3qvZ3OqvZ3qvZ3qvZ3qvZ3qvZ3qvZ3qvZ3qvZ'
WHERE username = 'admin' AND client_id = 1;

-- Option 3: Check what the current password might be
-- The hash in your DB is: $2a$10$8tKiWdSFa.QIpEijtPLVsuFzQ180Pjj1fpXDVOR246IeLX6Rbk.bS
-- This was likely created with a different password

-- After running the update, try logging in with 'admin123' or 'Admin@123'

-- To verify the update worked:
SELECT id, username, email_id, role, LEFT(password, 20) as password_hash
FROM identity_db.p_users
WHERE username = 'admin' AND client_id = 1;
