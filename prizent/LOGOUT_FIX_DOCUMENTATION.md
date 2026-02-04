# Login/Logout History Fix

## Problem
The `p_login_logout_histories` table in the `identity_db` database was not storing any logout details. All entries had NULL values in the `logout_date_time` column.

## Root Cause
The application was missing:
1. Entity class for LoginLogoutHistory
2. Repository interface for database operations
3. Logic to save login events
4. Logic to update logout time when users logout

## Solution Implemented

### Backend Changes (Java/Spring Boot)

#### 1. Created LoginLogoutHistory Entity
**File:** `backend/identity-service/src/main/java/com/elowen/identity/entity/LoginLogoutHistory.java`
- Maps to `p_login_logout_histories` table
- Fields: id, clientId, userId, userName, loginDateTime, logoutDateTime
- Uses UUID for primary key (BINARY(16))
- Auto-populates loginDateTime on creation

#### 2. Created LoginLogoutHistoryRepository
**File:** `backend/identity-service/src/main/java/com/elowen/identity/repository/LoginLogoutHistoryRepository.java`
- JPA repository for database operations
- Custom queries to find active sessions
- Method to find latest active login for a user
- Query to update logout time

#### 3. Updated AuthController
**File:** `backend/identity-service/src/main/java/com/elowen/identity/controller/AuthController.java`

**Login Changes:**
- Injected `LoginLogoutHistoryRepository`
- After successful authentication, creates new `LoginLogoutHistory` record
- Saves login timestamp automatically

**Logout Changes:**
- Updated logout endpoint to accept Authorization header
- Extracts user and client info from JWT token
- Finds the latest active session (where logoutDateTime is NULL)
- Updates logoutDateTime with current timestamp

#### 4. Enhanced JwtUtil
**File:** `backend/identity-service/src/main/java/com/elowen/identity/security/JwtUtil.java`
- Added `extractClientId()` method
- Added `extractUsername()` method
- Added `extractAllClaims()` method
- These methods help extract user info from JWT token during logout

### Frontend Changes (React/TypeScript)

#### 1. Updated authService
**File:** `prizent/src/services/authService.ts`
- Modified `logout()` to be async
- Now calls backend `/auth/logout` endpoint with Authorization header
- Sends JWT token in request for user identification
- Clears localStorage after backend call completes

#### 2. Updated CommonNavbar
**File:** `prizent/src/components/CommonNavbar.tsx`
- Changed `handleLogout()` to async function
- Awaits the logout service call before navigation

## How It Works Now

### Login Flow:
1. User submits credentials
2. Backend validates credentials
3. JWT token is generated
4. **NEW:** Login record created in `p_login_logout_histories` with current timestamp
5. Token sent to frontend

### Logout Flow:
1. User clicks logout button
2. **NEW:** Frontend calls backend `/auth/logout` with JWT token
3. **NEW:** Backend extracts userId and clientId from token
4. **NEW:** Backend finds the latest active session (NULL logout time)
5. **NEW:** Backend updates logoutDateTime with current timestamp
6. Frontend clears localStorage and redirects to login

## Database Schema
The table structure remains the same:
```sql
CREATE TABLE p_login_logout_histories (
    id BINARY(16) PRIMARY KEY,
    client_id INT NOT NULL,
    user_id BINARY(16) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    login_date_time DATETIME NOT NULL,
    logout_date_time DATETIME NULL
);
```

## Testing
To verify the fix:
1. Start the backend service
2. Login with a user account
3. Check the database - a new record should appear with login_date_time
4. Logout from the application
5. Check the database - the same record should now have logout_date_time populated

## Notes
- The logout endpoint returns success even if no active session is found (graceful handling)
- If backend logout call fails, frontend still clears local storage (fail-safe)
- Uses tenant isolation (clientId) to ensure data separation
- Only the most recent active session is updated on logout
