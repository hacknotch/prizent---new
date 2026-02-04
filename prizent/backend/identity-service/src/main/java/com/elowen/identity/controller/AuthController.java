package com.elowen.identity.controller;

import com.elowen.identity.dto.ChangePasswordRequest;
import com.elowen.identity.dto.ForgotPasswordRequest;
import com.elowen.identity.dto.LoginRequest;
import com.elowen.identity.dto.LoginResponse;
import com.elowen.identity.dto.ResetPasswordRequest;
import com.elowen.identity.entity.Client;
import com.elowen.identity.entity.LoginLogoutHistory;
import com.elowen.identity.entity.User;
import com.elowen.identity.repository.ClientRepository;
import com.elowen.identity.repository.LoginLogoutHistoryRepository;
import com.elowen.identity.repository.UserRepository;
import com.elowen.identity.security.JwtUtil;
import com.elowen.identity.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LoginLogoutHistoryRepository loginLogoutHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("Username/Email: " + request.getUsername());
            System.out.println("Password received: [" + request.getPassword() + "] (length: " + request.getPassword().length() + ")");
            
            // Find Test Client
            Optional<Client> clientOpt = clientRepository.findByName("Test Client");
            if (clientOpt.isEmpty()) {
                System.out.println("ERROR: Client 'Test Client' not found");
                LoginResponse response = new LoginResponse(false, "Client not found", null);
                return ResponseEntity.status(401).body(response);
            }

            Client client = clientOpt.get();
            System.out.println("Client found: " + client.getName() + " (ID: " + client.getId() + ")");

            // Find user by username or email
            Optional<User> userOpt = userRepository.findByUsernameOrEmailAndClientId(
                request.getUsername(), client.getId());
            
            if (userOpt.isEmpty()) {
                System.out.println("ERROR: User not found with identifier: " + request.getUsername() + " and client ID: " + client.getId());
                LoginResponse response = new LoginResponse(false, "Invalid credentials", null);
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername() + " (Email: " + user.getEmailId() + ")");
            System.out.println("Password hash in DB: " + user.getPassword());

            // Check password
            System.out.println("Checking password...");
            System.out.println("Comparing: '" + request.getPassword() + "' with hash");
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("ERROR: Password mismatch");
                LoginResponse response = new LoginResponse(false, "Invalid credentials", null);
                return ResponseEntity.status(401).body(response);
            }
            System.out.println("Password matched successfully");
            System.out.println("Password matched successfully");

            // Check if enabled
            System.out.println("Checking if account is enabled...");
            if (!user.getEnabled()) {
                System.out.println("ERROR: Account is disabled");
                LoginResponse response = new LoginResponse(false, "Account is disabled", null);
                return ResponseEntity.status(403).body(response);
            }
            System.out.println("Account is enabled");

            // Generate token
            System.out.println("Generating JWT token...");
            String token = jwtUtil.generateToken(
                client.getId().toString(),
                user.getId().toString(),
                user.getUsername(),
                user.getRole().toString()
            );
            System.out.println("Token generated successfully");

            // Save login history
            try {
                LoginLogoutHistory loginHistory = new LoginLogoutHistory(
                    client.getId(),
                    user.getId(),
                    user.getUsername()
                );
                loginLogoutHistoryRepository.save(loginHistory);
                System.out.println("Login history saved successfully");
            } catch (Exception e) {
                System.err.println("Failed to save login history: " + e.getMessage());
                e.printStackTrace();
            }

            // Prepare response
            LoginResponse response = new LoginResponse(true, "Login successful", token);
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId().toString(),
                user.getUsername(),
                client.getId().toString(),
                Arrays.asList("ROLE_" + user.getRole().toString())
            );
            response.setUser(userInfo);

            System.out.println("LOGIN SUCCESSFUL for user: " + user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERROR: Exception during login");
            e.printStackTrace();
            LoginResponse response = new LoginResponse(false, "Internal server error", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("\n\n");
        System.out.println("========================================");
        System.out.println("====== LOGOUT REQUEST RECEIVED =========");
        System.out.println("========================================");
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("Thread: " + Thread.currentThread().getName());
        
        try {
            System.out.println("\n--- Step 1: Check Authorization Header ---");
            System.out.println("Auth Header present: " + (authHeader != null));
            System.out.println("Auth Header value: " + authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("ERROR: No valid Bearer token in header");
                return ResponseEntity.ok("Logout successful (no session to end)");
            }

            System.out.println("\n--- Step 2: Extract Token ---");
            String token = authHeader.substring(7);
            System.out.println("Token length: " + token.length());
            System.out.println("Token preview: " + token.substring(0, Math.min(30, token.length())) + "...");
            
            System.out.println("\n--- Step 3: Validate Token ---");
            boolean isValid = jwtUtil.isTokenValid(token);
            System.out.println("Token valid: " + isValid);
            
            if (!isValid) {
                System.out.println("ERROR: Invalid token");
                return ResponseEntity.ok("Logout successful (invalid token)");
            }

            System.out.println("\n--- Step 4: Extract User Info ---");
            String userIdStr = jwtUtil.extractUserId(token);
            String clientIdStr = jwtUtil.extractClientId(token);
            System.out.println("User ID from token: " + userIdStr);
            System.out.println("Client ID from token: " + clientIdStr);

            Long userId = Long.parseLong(userIdStr);
            Integer clientId = Integer.parseInt(clientIdStr);
            System.out.println("Parsed - userId: " + userId + ", clientId: " + clientId);

            System.out.println("\n--- Step 5: Find Active Session ---");
            System.out.println("Searching for active session with clientId=" + clientId + ", userId=" + userId);
            Optional<LoginLogoutHistory> activeSession = loginLogoutHistoryRepository
                .findLatestActiveLoginByClientIdAndUserId(clientId, userId);

            System.out.println("Active session found: " + activeSession.isPresent());
            
            if (activeSession.isPresent()) {
                LoginLogoutHistory history = activeSession.get();
                System.out.println("\n--- Step 6: Update Session ---");
                System.out.println("Session ID: " + history.getId());
                System.out.println("User ID in session: " + history.getUserId());
                System.out.println("Client ID in session: " + history.getClientId());
                System.out.println("Login time: " + history.getLoginDateTime());
                System.out.println("Current logout time: " + history.getLogoutDateTime());
                
                LocalDateTime logoutTime = LocalDateTime.now();
                System.out.println("New logout time: " + logoutTime);
                System.out.println("Calling updateLogoutTime...");
                
                loginLogoutHistoryRepository.updateLogoutTime(history.getId(), logoutTime);
                
                System.out.println("Update completed successfully!");
                System.out.println("Session ID " + history.getId() + " should now have logout time: " + logoutTime);
            } else {
                System.out.println("\n--- ERROR: No Active Session Found ---");
                System.out.println("WARNING: No active session for userId=" + userId + ", clientId=" + clientId);
                
                // Debug: Check if ANY sessions exist for this user
                System.out.println("\nDEBUG: Checking all sessions for this user...");
                try {
                    java.util.List<LoginLogoutHistory> allSessions = loginLogoutHistoryRepository
                        .findByClientIdAndUserId(clientId, userId);
                    System.out.println("Total sessions found: " + allSessions.size());
                    for (LoginLogoutHistory s : allSessions) {
                        System.out.println("  - Session ID: " + s.getId() + 
                            ", Login: " + s.getLoginDateTime() + 
                            ", Logout: " + s.getLogoutDateTime());
                    }
                } catch (Exception e) {
                    System.out.println("Error checking all sessions: " + e.getMessage());
                }
            }

            System.out.println("\n========================================");
            System.out.println("====== LOGOUT COMPLETED ================");
            System.out.println("========================================\n\n");
            return ResponseEntity.ok("Logout successful");

        } catch (Exception e) {
            System.err.println("\n!!! LOGOUT ERROR !!!");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("!!! END ERROR !!!\n");
            return ResponseEntity.ok("Logout successful (with errors)");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String token = passwordResetService.generateResetToken(request.getUsernameOrEmail());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset token generated successfully");
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("FORGOT PASSWORD ERROR: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("RESET PASSWORD ERROR: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            if (!authHeader.startsWith("Bearer ")) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Invalid authorization header");
                return ResponseEntity.status(401).body(response);
            }

            String token = authHeader.substring(7);
            
            if (!jwtUtil.isTokenValid(token)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            // Extract user info from JWT
            String userIdStr = jwtUtil.extractUserId(token);
            String clientIdStr = jwtUtil.extractClientId(token);
            
            Long userId = Long.parseLong(userIdStr);
            Integer clientId = Integer.parseInt(clientIdStr);

            passwordResetService.changePassword(userId, clientId, request.getOldPassword(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("CHANGE PASSWORD ERROR: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/test-logout-update")
    @Transactional
    public ResponseEntity<Map<String, String>> testLogoutUpdate() {
        System.out.println("========== TEST LOGOUT UPDATE ==========");
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find ANY active login
            System.out.println("Finding all active logins...");
            java.util.List<LoginLogoutHistory> allActive = loginLogoutHistoryRepository
                .findActiveSessionsByClientId(1);
            
            System.out.println("Found " + allActive.size() + " active sessions");
            
            if (allActive.isEmpty()) {
                System.out.println("No active sessions found");
                response.put("status", "error");
                response.put("message", "No active sessions found");
                return ResponseEntity.ok(response);
            }
            
            // Use the first active session
            LoginLogoutHistory history = allActive.get(0);
            System.out.println("Using session ID: " + history.getId());
            System.out.println("User ID: " + history.getUserId());
            System.out.println("Client ID: " + history.getClientId());
            System.out.println("Login time: " + history.getLoginDateTime());
            System.out.println("Current logout time: " + history.getLogoutDateTime());
            
            LocalDateTime logoutTime = LocalDateTime.now();
            System.out.println("Calling updateLogoutTime with time: " + logoutTime);
            
            loginLogoutHistoryRepository.updateLogoutTime(history.getId(), logoutTime);
            
            System.out.println("Update called successfully");
            
            // Flush to ensure immediate persistence
            loginLogoutHistoryRepository.flush();
            System.out.println("Flushed");
            
            // Verify the update
            Optional<LoginLogoutHistory> updated = loginLogoutHistoryRepository.findById(history.getId());
            if (updated.isPresent()) {
                System.out.println("After update - Logout time: " + updated.get().getLogoutDateTime());
                response.put("status", "success");
                response.put("sessionId", history.getId().toString());
                response.put("userId", history.getUserId().toString());
                response.put("logoutTime", updated.get().getLogoutDateTime() != null ? 
                    updated.get().getLogoutDateTime().toString() : "NULL");
            } else {
                response.put("status", "error");
                response.put("message", "Could not find session after update");
            }
            
            System.out.println("========== TEST COMPLETED ==========");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("TEST ERROR: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
