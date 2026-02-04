-- ========================================
-- VERIFICATION SCRIPT - POST MIGRATION
-- ========================================
-- Run this after migration to verify no UUIDs remain
-- All queries should return ZERO rows or show only BIGINT/INT types
-- ========================================

USE identity_db;

-- ========================================
-- CHECK 1: Find any BINARY columns
-- ========================================
SELECT 
    'VIOLATION: BINARY column found' AS issue_type,
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND DATA_TYPE = 'binary';

-- Expected: 0 rows

-- ========================================
-- CHECK 2: Find any CHAR(36) columns (UUID string format)
-- ========================================
SELECT 
    'VIOLATION: CHAR(36) column found' AS issue_type,
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND COLUMN_TYPE LIKE '%CHAR(36)%';

-- Expected: 0 rows

-- ========================================
-- CHECK 3: Find any UUID data type
-- ========================================
SELECT 
    'VIOLATION: UUID data type found' AS issue_type,
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND DATA_TYPE = 'uuid';

-- Expected: 0 rows

-- ========================================
-- CHECK 4: List all ID columns and their types
-- ========================================
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    EXTRA,
    CASE 
        WHEN DATA_TYPE IN ('bigint', 'int', 'integer') THEN '✓ VALID'
        ELSE '✗ INVALID'
    END AS status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND (COLUMN_NAME = 'id' OR COLUMN_NAME LIKE '%_id')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- All should show "✓ VALID"

-- ========================================
-- CHECK 5: Verify foreign key consistency
-- ========================================
SELECT 
    kcu.TABLE_NAME,
    kcu.COLUMN_NAME AS fk_column,
    kcu.REFERENCED_TABLE_NAME,
    kcu.REFERENCED_COLUMN_NAME,
    c1.DATA_TYPE AS fk_type,
    c2.DATA_TYPE AS referenced_type,
    CASE 
        WHEN c1.DATA_TYPE = c2.DATA_TYPE THEN '✓ MATCH'
        ELSE '✗ MISMATCH'
    END AS type_match
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
JOIN INFORMATION_SCHEMA.COLUMNS c1 
    ON kcu.TABLE_SCHEMA = c1.TABLE_SCHEMA 
    AND kcu.TABLE_NAME = c1.TABLE_NAME 
    AND kcu.COLUMN_NAME = c1.COLUMN_NAME
JOIN INFORMATION_SCHEMA.COLUMNS c2 
    ON kcu.REFERENCED_TABLE_SCHEMA = c2.TABLE_SCHEMA 
    AND kcu.REFERENCED_TABLE_NAME = c2.TABLE_NAME 
    AND kcu.REFERENCED_COLUMN_NAME = c2.COLUMN_NAME
WHERE kcu.TABLE_SCHEMA = 'identity_db'
AND kcu.REFERENCED_TABLE_NAME IS NOT NULL;

-- All should show "✓ MATCH"

-- ========================================
-- CHECK 6: Table structure summary
-- ========================================
SELECT 
    '========== p_users ==========' AS table_info;

DESCRIBE p_users;

SELECT 
    '========== p_clients ==========' AS table_info;

DESCRIBE p_clients;

SELECT 
    '========== p_login_logout_histories ==========' AS table_info;

DESCRIBE p_login_logout_histories;

SELECT 
    '========== p_password_recovery_histories ==========' AS table_info;

DESCRIBE p_password_recovery_histories;

-- ========================================
-- CHECK 7: Count records after migration
-- ========================================
SELECT 
    'p_users' AS table_name,
    COUNT(*) AS record_count
FROM p_users
UNION ALL
SELECT 
    'p_clients' AS table_name,
    COUNT(*) AS record_count
FROM p_clients
UNION ALL
SELECT 
    'p_login_logout_histories' AS table_name,
    COUNT(*) AS record_count
FROM p_login_logout_histories
UNION ALL
SELECT 
    'p_password_recovery_histories' AS table_name,
    COUNT(*) AS record_count
FROM p_password_recovery_histories;

-- ========================================
-- FINAL SUMMARY REPORT
-- ========================================
SELECT 
    CONCAT(
        'Migration Status: ',
        CASE 
            WHEN (
                SELECT COUNT(*) 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'identity_db'
                AND (
                    DATA_TYPE = 'binary' 
                    OR COLUMN_TYPE LIKE '%BINARY(16)%'
                    OR COLUMN_TYPE LIKE '%CHAR(36)%'
                    OR DATA_TYPE = 'uuid'
                )
            ) = 0 
            THEN '✓ SUCCESS - No UUID columns remain'
            ELSE '✗ FAILED - UUID columns still exist'
        END
    ) AS final_status;
