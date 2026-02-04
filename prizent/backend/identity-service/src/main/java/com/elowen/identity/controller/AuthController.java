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
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("LOGOUT: No token provided or invalid format");
                return ResponseEntity.ok("Logout successful (no session to end)");
            }

            String token = authHeader.substring(7);
            
            // Validate token
            if (!jwtUtil.isTokenValid(token)) {
                System.out.println("LOGOUT: Invalid token");
                return ResponseEntity.ok("Logout successful (invalid token)");
            }

            // Extract user info from token
            String userIdStr = jwtUtil.extractUserId(token);
            String clientIdStr = jwtUtil.extractClientId(token);
            
            System.out.println("LOGOUT: User ID: " + userIdStr + ", Client ID: " + clientIdStr);

            Long userId = Long.parseLong(userIdStr);
            Integer clientId = Integer.parseInt(clientIdStr);

            // Find the latest active login session
            Optional<LoginLogoutHistory> activeSession = loginLogoutHistoryRepository
                .findLatestActiveLoginByClientIdAndUserId(clientId, userId);

            if (activeSession.isPresent()) {
                LoginLogoutHistory history = activeSession.get();
                history.setLogoutDateTime(LocalDateTime.now());
                loginLogoutHistoryRepository.save(history);
                System.out.println("LOGOUT: Logout time updated for session ID: " + history.getId());
            } else {
                System.out.println("LOGOUT: No active session found for user");
            }

            return ResponseEntity.ok("Logout successful");

        } catch (Exception e) {
            System.err.println("LOGOUT ERROR: " + e.getMessage());
            e.printStackTrace();
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
}
