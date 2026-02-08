package com.elowen.identity.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this as a standalone Java application to generate hashes for passwords
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for 'newpassword123'
        String password1 = "newpassword123";
        String hash1 = encoder.encode(password1);
        System.out.println("Password: " + password1);
        System.out.println("Hash: " + hash1);
        System.out.println();
        
        // Generate hash for 'admin123'
        String password2 = "admin123";
        String hash2 = encoder.encode(password2);
        System.out.println("Password: " + password2);
        System.out.println("Hash: " + hash2);
        System.out.println();
        
        // Test if the current hash matches 'newpassword123'
        String existingHash = "$2a$10$8tKiWdSFa.QIpEijtPLVsuFzQ180Pjj1fpXDVOR246IeLX6Rbk.bS";
        boolean matches = encoder.matches(password1, existingHash);
        System.out.println("Does 'newpassword123' match existing hash? " + matches);
        
        // Try common passwords
        String[] commonPasswords = {"admin", "admin123", "password", "Password123", "Admin123"};
        System.out.println("\nTesting common passwords against existing hash:");
        for (String pwd : commonPasswords) {
            boolean match = encoder.matches(pwd, existingHash);
            System.out.println(pwd + ": " + match);
        }
    }
}
