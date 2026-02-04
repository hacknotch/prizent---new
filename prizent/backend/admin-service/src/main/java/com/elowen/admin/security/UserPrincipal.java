package com.elowen.admin.security;

/**
 * Principal class to hold authenticated user details extracted from JWT
 * Updated to use String for userId (UUID) and Integer for clientId
 */
public class UserPrincipal {
    
    private final String userId;
    private final Integer clientId;
    private final String username;
    private final String role;

    public UserPrincipal(String userId, Integer clientId, String username, String role) {
        this.userId = userId;
        this.clientId = clientId;
        this.username = username;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId=" + userId +
                ", clientId=" + clientId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
