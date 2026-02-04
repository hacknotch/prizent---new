package com.elowen.identity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_password_recovery_histories")
public class PasswordRecoveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Client ID is required")
    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Old password is required")
    @Column(name = "old_password", nullable = false, length = 255)
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Column(name = "new_password", nullable = false, length = 255)
    private String newPassword;

    @Column(name = "changed_time_date", nullable = false)
    private LocalDateTime changedTimeDate;

    // Constructors
    public PasswordRecoveryHistory() {}

    public PasswordRecoveryHistory(Integer clientId, Long userId, String oldPassword, String newPassword) {
        this.clientId = clientId;
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.changedTimeDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (changedTimeDate == null) {
            changedTimeDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public LocalDateTime getChangedTimeDate() {
        return changedTimeDate;
    }

    public void setChangedTimeDate(LocalDateTime changedTimeDate) {
        this.changedTimeDate = changedTimeDate;
    }

    @Override
    public String toString() {
        return "PasswordRecoveryHistory{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", userId=" + userId +
                ", changedTimeDate=" + changedTimeDate +
                '}';
    }
}
