package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Attribute entity representing client-owned reusable attributes.
 * 
 * Attributes define characteristics that can be assigned to categories
 * and subsequently applied to products (e.g., Size, Color, Material).
 * 
 * Key Design:
 * - Uses INTEGER for id (no UUIDs)
 * - client_id is immutable and extracted from UserPrincipal
 * - Unique constraint on (client_id, name) - name unique per client
 * - Soft delete pattern using 'enabled' field
 * - Reusable across multiple categories
 */
@Entity
@Table(
    name = "p_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attributes_client_name",
            columnNames = {"client_id", "name"}
        )
    },
    indexes = {
        @Index(name = "idx_attributes_client_id", columnList = "client_id"),
        @Index(name = "idx_attributes_enabled", columnList = "enabled")
    }
)
public class Attribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;
    
    /**
     * Client ID - IMMUTABLE after creation
     * Always extracted from UserPrincipal, never from request
     */
    @NotNull
    @Column(name = "client_id", updatable = false, nullable = false)
    private Integer clientId;
    
    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    /**
     * Soft delete flag - true = active, false = disabled
     * Default: true (active)
     */
    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @CreationTimestamp
    @Column(name = "create_date_time", updatable = false, nullable = false)
    private LocalDateTime createDateTime;
    
    @UpdateTimestamp
    @Column(name = "update_date_time", nullable = false)
    private LocalDateTime updateDateTime;
    
    // Constructors
    public Attribute() {}
    
    public Attribute(Integer clientId, String name) {
        this.clientId = clientId;
        this.name = name;
        this.enabled = true;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
        return "Attribute{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
