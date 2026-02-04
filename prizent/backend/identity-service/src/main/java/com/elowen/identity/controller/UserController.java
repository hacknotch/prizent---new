package com.elowen.identity.controller;

import com.elowen.identity.dto.CreateUserRequest;
import com.elowen.identity.dto.UpdateUserRequest;
import com.elowen.identity.dto.UserResponse;
import com.elowen.identity.entity.User;
import com.elowen.identity.repository.UserRepository;
import com.elowen.identity.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for User Management operations
 * Handles CRUD operations for users
 */
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all users for a client
     * GET /api/admin/users?clientId={clientId}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(@RequestParam Integer clientId) {
        System.out.println("GET ALL USERS request for clientId: " + clientId);
        
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getClientId().equals(clientId))
                .collect(Collectors.toList());
        
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Users retrieved successfully");
        response.put("users", userResponses);
        response.put("count", userResponses.size());
        
        System.out.println("Retrieved " + userResponses.size() + " users for clientId: " + clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        System.out.println("GET USER BY ID request for userId: " + userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        UserResponse userResponse = new UserResponse(userOpt.get());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User retrieved successfully");
        response.put("user", userResponse);
        
        System.out.println("User retrieved: " + userResponse.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new user
     * POST /api/admin/users
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestParam Integer clientId) {
        
        System.out.println("CREATE USER request for clientId: " + clientId);
        System.out.println("Username: " + request.getUsername());
        
        // Check if username already exists
        Optional<User> existingUser = userRepository.findByUsernameAndClientId(
                request.getUsername(), clientId);
        
        if (existingUser.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmailId(request.getEmailId());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmployeeDesignation(request.getEmployeeDesignation());
        user.setClientId(clientId);
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : false);
        
        // Set role
        try {
            user.setRole(User.Role.valueOf(request.getRole()));
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid role. Valid roles are: ADMIN, USER, SUPER_ADMIN");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(savedUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User created successfully");
        response.put("user", userResponse);
        
        System.out.println("User created: ID " + savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a user
     * PUT /api/admin/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        System.out.println("UPDATE USER request for userId: " + userId);
        
        // Extract updater's user ID from JWT token
        Long updatedBy = extractUserIdFromToken(authHeader);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        User user = userOpt.get();
        
        // Update fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Check if new username already exists for another user
            Optional<User> existingUser = userRepository.findByUsernameAndClientId(
                    request.getUsername(), user.getClientId());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmailId() != null) {
            user.setEmailId(request.getEmailId());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmployeeDesignation() != null) {
            user.setEmployeeDesignation(request.getEmployeeDesignation());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid role. Valid roles are: ADMIN, USER, SUPER_ADMIN");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        
        // Set updated_by
        if (updatedBy != null) {
            user.setUpdatedBy(updatedBy);
        }
        
        User updatedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(updatedUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User updated successfully");
        response.put("user", userResponse);
        
        System.out.println("User updated: ID " + updatedUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Enable user
     * PATCH /api/admin/users/{userId}/enable
     */
    @PatchMapping("/{userId}/enable")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable Long userId) {
        System.out.println("ENABLE USER request for userId: " + userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        User user = userOpt.get();
        user.setEnabled(true);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User enabled successfully");
        
        System.out.println("User enabled: ID " + userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Disable user
     * PATCH /api/admin/users/{userId}/disable
     */
    @PatchMapping("/{userId}/disable")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable Long userId) {
        System.out.println("DISABLE USER request for userId: " + userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        User user = userOpt.get();
        user.setEnabled(false);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User disabled successfully");
        
        System.out.println("User disabled: ID " + userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        System.out.println("DELETE USER request for userId: " + userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        userRepository.deleteById(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        
        System.out.println("User deleted: ID " + userId);
        return ResponseEntity.ok(response);
    }
    /**
     * Helper method to extract userId from JWT token
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String userIdStr = jwtUtil.extractUserId(token);
                return Long.parseLong(userIdStr);
            } catch (Exception e) {
                System.err.println("Error extracting userId from token: " + e.getMessage());
            }
        }
        return null;
    }}
