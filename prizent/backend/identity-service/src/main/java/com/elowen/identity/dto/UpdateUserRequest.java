package com.elowen.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String emailId;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Employee designation must not exceed 100 characters")
    private String employeeDesignation;

    private String role; // ADMIN, USER, SUPER_ADMIN

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional - only update if provided

    private Boolean enabled; // User enabled status

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmployeeDesignation() {
        return employeeDesignation;
    }

    public void setEmployeeDesignation(String employeeDesignation) {
        this.employeeDesignation = employeeDesignation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
