# UUID to BIGINT Database Migration

## Overview
This directory contains SQL scripts to migrate the `identity_db` database from UUID/BINARY(16) to BIGINT for all ID columns.

## ⚠️ Critical Information

**BEFORE RUNNING ANY SCRIPT:**
1. **Backup your database completely**
2. Stop all application services
3. Test on a staging environment first
4. Schedule downtime for production

## Migration Scripts

### 1. `01_uuid_to_bigint_migration.sql` (Development/Testing)
**Use Case:** Fresh start, no critical data to preserve

**What it does:**
- Truncates all affected tables (DELETES ALL DATA)
- Converts all UUID columns to BIGINT
- Re-establishes foreign key constraints
- Adds performance indexes

**Tables affected:**
- `p_users`: `id` BINARY(16) → BIGINT
- `p_login_logout_histories`: `id` and `user_id` BINARY(16) → BIGINT
- `p_password_recovery_histories`: `user_id` BINARY(16) → BIGINT
- `p_clients`: Already INT (no changes)

**Run with:**
```bash
mysql -u root -p identity_db < 01_uuid_to_bigint_migration.sql
```

---

### 2. `02_verification_queries.sql`
**Use Case:** Verify migration success

**What it does:**
- Checks for any remaining UUID/BINARY columns
- Validates foreign key type consistency
- Shows table structures
- Generates final status report

**Run with:**
```bash
mysql -u root -p identity_db < 02_verification_queries.sql
```

**Expected result:** All checks pass, zero UUID violations

---

### 3. `03_production_migration_with_data.sql` (Production)
**Use Case:** Preserve existing data during migration

**What it does:**
- Creates new BIGINT columns alongside old UUID columns
- Generates deterministic UUID → BIGINT mapping
- Migrates data with referential integrity
- Swaps columns atomically
- Preserves mapping table for audit trail

**Run with:**
```bash
mysql -u root -p identity_db < 03_production_migration_with_data.sql
```

**Note:** Creates `uuid_to_bigint_mapping` table for reference

---

### 4. `04_rollback.sql` (Emergency Only)
**Use Case:** Restore previous state if migration fails

**Prerequisites:**
- Must have created backup tables before migration
- Only works if you ran backup commands

**Run with:**
```bash
mysql -u root -p identity_db < 04_rollback.sql
```

---

## Migration Workflow

### For Development/Testing Environment:
```bash
# 1. Backup (optional but recommended)
mysqldump -u root -p identity_db > identity_db_backup.sql

# 2. Run migration
mysql -u root -p identity_db < 01_uuid_to_bigint_migration.sql

# 3. Verify
mysql -u root -p identity_db < 02_verification_queries.sql

# 4. Restart application and test
```

### For Production Environment:
```bash
# 1. MANDATORY: Full database backup
mysqldump -u root -p identity_db > identity_db_backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Stop all services
systemctl stop identity-service

# 3. Run production migration
mysql -u root -p identity_db < 03_production_migration_with_data.sql

# 4. Verify migration
mysql -u root -p identity_db < 02_verification_queries.sql

# 5. If verification passes, restart services
systemctl start identity-service

# 6. Test application thoroughly

# 7. If all good, optionally drop mapping table
mysql -u root -p -e "DROP TABLE identity_db.uuid_to_bigint_mapping;"
```

---

## Verification Checklist

After running migration, verify:

✅ **No UUID columns remain:**
```sql
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND (DATA_TYPE = 'binary' OR COLUMN_TYPE LIKE '%BINARY(16)%');
-- Expected: 0
```

✅ **All ID columns are numeric:**
```sql
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND COLUMN_NAME LIKE '%id%';
-- All should show 'bigint' or 'int'
```

✅ **Foreign keys are consistent:**
```sql
-- Run verification script to check FK type matching
```

✅ **Application tests pass:**
- Login/Logout works
- Password reset works
- User creation works
- All API endpoints return numeric IDs

---

## Common Issues & Solutions

### Issue: "Cannot add foreign key constraint"
**Cause:** Data type mismatch between FK and PK
**Solution:** Ensure both columns are BIGINT

### Issue: "Column 'id' cannot be null"
**Cause:** AUTO_INCREMENT not set properly
**Solution:** Re-run the column definition with AUTO_INCREMENT

### Issue: Application throws parsing errors
**Cause:** Application code still expects UUID
**Solution:** Ensure all Java entities are updated to use Long/Integer

---

## Post-Migration

### Database Changes:
- All ID columns: BIGINT AUTO_INCREMENT
- All foreign keys: BIGINT (matching parent table)
- Indexes added for performance
- Constraints restored

### Application Changes Required:
- All entities use `Long` for ID fields
- All repositories use `Long` as generic type parameter
- JWT tokens contain numeric IDs as strings
- API responses return numeric IDs

### Performance Impact:
- ✅ BIGINT is more efficient than BINARY(16)
- ✅ Smaller index size
- ✅ Faster joins
- ✅ Better database compatibility

---

## Support

If migration fails:
1. Check error logs carefully
2. Restore from backup
3. Review table constraints
4. Ensure no active connections during migration
5. Test on smaller dataset first

---

## Migration Status Tracking

| Table | Old Type | New Type | Status |
|-------|----------|----------|--------|
| p_users.id | BINARY(16) | BIGINT | ✅ Migrated |
| p_login_logout_histories.id | BINARY(16) | BIGINT | ✅ Migrated |
| p_login_logout_histories.user_id | BINARY(16) | BIGINT | ✅ Migrated |
| p_password_recovery_histories.user_id | BINARY(16) | BIGINT | ✅ Migrated |
| p_clients.id | INT | INT | ✅ No Change |

---

## Final Verification Command

```bash
mysql -u root -p identity_db -e "
SELECT 
    CASE 
        WHEN COUNT(*) = 0 
        THEN '✓ UUID MIGRATION COMPLETE - All IDs are numeric'
        ELSE '✗ MIGRATION FAILED - UUID columns remain'
    END AS final_status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'identity_db'
AND (
    DATA_TYPE = 'binary' 
    OR COLUMN_TYPE LIKE '%BINARY(16)%'
    OR COLUMN_TYPE LIKE '%CHAR(36)%'
    OR DATA_TYPE = 'uuid'
);"
```

Expected output:
```
✓ UUID MIGRATION COMPLETE - All IDs are numeric
```
