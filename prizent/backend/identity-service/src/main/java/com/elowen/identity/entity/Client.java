package com.elowen.identity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_clients", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "uk_client_name"),
    @UniqueConstraint(columnNames = {"client_domain"}, name = "uk_client_domain")
})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Client name is required")
    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    @NotBlank(message = "Client domain is required")
    @Column(name = "client_domain", nullable = false, unique = true, length = 100)
    private String clientDomain;

    @NotNull(message = "Number of users allowed is required")
    @Min(value = 1, message = "Number of users allowed must be at least 1")
    @Column(name = "number_of_users_allowed", nullable = false)
    private Integer numberOfUsersAllowed;

    @Column(name = "logo", length = 500)
    private String logo;

    @NotNull(message = "Enabled status is required")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @Column(name = "update_date_time", nullable = false)
    private LocalDateTime updateDateTime;

    // Constructors
    public Client() {}

    public Client(String name, String clientDomain, Integer numberOfUsersAllowed, String logo, Boolean enabled) {
        this.name = name;
        this.clientDomain = clientDomain;
        this.numberOfUsersAllowed = numberOfUsersAllowed;
        this.logo = logo;
        this.enabled = enabled != null ? enabled : true;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientDomain() {
        return clientDomain;
    }

    public void setClientDomain(String clientDomain) {
        this.clientDomain = clientDomain;
    }

    public Integer getNumberOfUsersAllowed() {
        return numberOfUsersAllowed;
    }

    public void setNumberOfUsersAllowed(Integer numberOfUsersAllowed) {
        this.numberOfUsersAllowed = numberOfUsersAllowed;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", clientDomain='" + clientDomain + '\'' +
                ", numberOfUsersAllowed=" + numberOfUsersAllowed +
                ", enabled=" + enabled +
                '}';
    }
}
