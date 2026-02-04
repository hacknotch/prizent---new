package com.elowen.identity.service;

import com.elowen.identity.entity.Client;
import com.elowen.identity.entity.PasswordRecoveryHistory;
import com.elowen.identity.entity.User;
import com.elowen.identity.repository.ClientRepository;
import com.elowen.identity.repository.PasswordRecoveryHistoryRepository;
import com.elowen.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordRecoveryHistoryRepository passwordHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In-memory token storage: token -> TokenData
    private final ConcurrentHashMap<String, TokenData> resetTokens = new ConcurrentHashMap<>();

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int PASSWORD_HISTORY_CHECK = 3;

    /**
     * Inner class to store token data
     */
    private static class TokenData {
        Long userId;
        Integer clientId;
        LocalDateTime expiryTime;

        TokenData(Long userId, Integer clientId, LocalDateTime expiryTime) {
            this.userId = userId;
            this.clientId = clientId;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    /**
     * Generate reset token for forgot password
     */
    public String generateResetToken(String usernameOrEmail) {
        System.out.println("=== FORGOT PASSWORD REQUEST ===");
        System.out.println("Username/Email: " + usernameOrEmail);

        // Find Test Client (hardcoded for now, can be made dynamic)
        Optional<Client> clientOpt = clientRepository.findByName("Test Client");
        if (clientOpt.isEmpty()) {
            System.out.println("ERROR: Client 'Test Client' not found");
            throw new RuntimeException("Client not found");
        }

        Client client = clientOpt.get();
        if (!client.getEnabled()) {
            System.out.println("ERROR: Client is disabled");
            throw new RuntimeException("Client is disabled");
        }

        // Find user by username or email
        Optional<User> userOpt = userRepository.findByUsernameOrEmailAndClientId(usernameOrEmail, client.getId());
        if (userOpt.isEmpty()) {
            System.out.println("ERROR: User not found");
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        if (!user.getEnabled()) {
            System.out.println("ERROR: User account is disabled");
            throw new RuntimeException("User account is disabled");
        }

        // Generate secure random token
        String token = generateSecureToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

        // Store token in memory
        resetTokens.put(token, new TokenData(user.getId(), client.getId(), expiryTime));

        System.out.println("✓ Reset token generated for user: " + user.getUsername());
        System.out.println("Token expires at: " + expiryTime);

        return token;
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        System.out.println("=== RESET PASSWORD REQUEST ===");
        System.out.println("Token: " + token);

        // Validate token exists
        TokenData tokenData = resetTokens.get(token);
        if (tokenData == null) {
            System.out.println("ERROR: Invalid or expired token");
            throw new RuntimeException("Invalid or expired reset token");
        }

        // Check token expiry
        if (tokenData.isExpired()) {
            resetTokens.remove(token);
            System.out.println("ERROR: Token has expired");
            throw new RuntimeException("Reset token has expired");
        }

        // Find user
        Optional<User> userOpt = userRepository.findById(tokenData.userId);
        if (userOpt.isEmpty()) {
            resetTokens.remove(token);
            System.out.println("ERROR: User not found");
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        String oldPasswordHash = user.getPassword();

        // Validate new password is different from old
        if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
            System.out.println("ERROR: New password cannot be the same as old password");
            throw new RuntimeException("New password cannot be the same as old password");
        }

        // Check password history (prevent reuse of last 3 passwords)
        List<PasswordRecoveryHistory> history = passwordHistoryRepository
            .findLastNPasswordsByClientIdAndUserId(tokenData.clientId, tokenData.userId, PASSWORD_HISTORY_CHECK);
        
        for (PasswordRecoveryHistory record : history) {
            if (passwordEncoder.matches(newPassword, record.getNewPassword())) {
                System.out.println("ERROR: Password was used recently. Cannot reuse last " + PASSWORD_HISTORY_CHECK + " passwords");
                throw new RuntimeException("Password was used recently. Please choose a different password");
            }
        }

        // Encrypt new password
        String newPasswordHash = passwordEncoder.encode(newPassword);

        // Update user password
        user.setPassword(newPasswordHash);
        userRepository.save(user);
        System.out.println("✓ User password updated");

        // Save password history
        PasswordRecoveryHistory historyRecord = new PasswordRecoveryHistory(
            tokenData.clientId,
            tokenData.userId,
            oldPasswordHash,
            newPasswordHash
        );
        passwordHistoryRepository.save(historyRecord);
        System.out.println("✓ Password history recorded");

        // Invalidate token (single use)
        resetTokens.remove(token);
        System.out.println("✓ Reset token invalidated");
    }

    /**
     * Change password for logged-in user
     */
    @Transactional
    public void changePassword(Long userId, Integer clientId, String oldPassword, String newPassword) {
        System.out.println("=== CHANGE PASSWORD REQUEST ===");
        System.out.println("User ID: " + userId);

        // Find user
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("ERROR: User not found");
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Verify client_id matches (tenant isolation)
        if (!user.getClientId().equals(clientId)) {
            System.out.println("ERROR: Client ID mismatch");
            throw new RuntimeException("Unauthorized access");
        }

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            System.out.println("ERROR: Old password is incorrect");
            throw new RuntimeException("Old password is incorrect");
        }

        String oldPasswordHash = user.getPassword();

        // Validate new password is different from old
        if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
            System.out.println("ERROR: New password cannot be the same as old password");
            throw new RuntimeException("New password cannot be the same as old password");
        }

        // Check password history (prevent reuse of last 3 passwords)
        List<PasswordRecoveryHistory> history = passwordHistoryRepository
            .findLastNPasswordsByClientIdAndUserId(clientId, userId, PASSWORD_HISTORY_CHECK);
        
        for (PasswordRecoveryHistory record : history) {
            if (passwordEncoder.matches(newPassword, record.getNewPassword())) {
                System.out.println("ERROR: Password was used recently. Cannot reuse last " + PASSWORD_HISTORY_CHECK + " passwords");
                throw new RuntimeException("Password was used recently. Please choose a different password");
            }
        }

        // Encrypt new password
        String newPasswordHash = passwordEncoder.encode(newPassword);

        // Update user password
        user.setPassword(newPasswordHash);
        userRepository.save(user);
        System.out.println("✓ User password updated");

        // Save password history
        PasswordRecoveryHistory historyRecord = new PasswordRecoveryHistory(
            clientId,
            userId,
            oldPasswordHash,
            newPasswordHash
        );
        passwordHistoryRepository.save(historyRecord);
        System.out.println("✓ Password history recorded");
    }

    /**
     * Clean up expired tokens (can be called periodically)
     */
    public void cleanupExpiredTokens() {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Generate secure random token string
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
