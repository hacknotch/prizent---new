package com.elowen.identity.dto;

import com.elowen.identity.entity.User;

public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String emailId;
    private String phoneNumber;
    private String employeeDesignation;
    private String role;
    private Boolean enabled;
    private Integer clientId;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.emailId = user.getEmailId();
        this.phoneNumber = user.getPhoneNumber();
        this.employeeDesignation = user.getEmployeeDesignation();
        this.role = user.getRole().name();
        this.enabled = user.getEnabled();
        this.clientId = user.getClientId();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
}
