package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AttributeValue entity representing predefined values for attributes.
 * 
 * Examples:
 * - Attribute: Size → Values: S, M, L, XL
 * - Attribute: Fabric → Values: Cotton, Silk, Polyester
 * - Attribute: Color → Values: Red, Blue, Green
 * 
 * Key Design:
 * - Uses INTEGER for all IDs (no UUIDs)
 * - client_id is immutable and extracted from UserPrincipal
 * - attribute_id links to parent attribute
 * - Unique constraint on (client_id, attribute_id, value) - case-sensitive in DB
 * - Case-insensitive uniqueness enforced in service layer
 * - Soft delete pattern using 'enabled' field
 */
@Entity
@Table(
    name = "p_attribute_values",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attribute_values_client_attr_value",
            columnNames = {"client_id", "attribute_id", "value"}
        )
    },
    indexes = {
        @Index(name = "idx_attribute_values_client_id", columnList = "client_id"),
        @Index(name = "idx_attribute_values_attribute_id", columnList = "attribute_id"),
        @Index(name = "idx_attribute_values_enabled", columnList = "enabled")
    }
)
public class AttributeValue {
    
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
    
    /**
     * Attribute ID - links to p_attributes
     * IMMUTABLE after creation
     */
    @NotNull
    @Column(name = "attribute_id", updatable = false, nullable = false)
    private Integer attributeId;
    
    /**
     * The actual value (e.g., "S", "Cotton", "Red")
     * Case-sensitive in DB, case-insensitive uniqueness checked in service
     */
    @NotBlank
    @Column(name = "value", nullable = false, length = 255)
    private String value;
    
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
    public AttributeValue() {}
    
    public AttributeValue(Integer clientId, Integer attributeId, String value) {
        this.clientId = clientId;
        this.attributeId = attributeId;
        this.value = value;
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
    
    public Integer getAttributeId() {
        return attributeId;
    }
    
    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
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
        return "AttributeValue{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", attributeId=" + attributeId +
                ", value='" + value + '\'' +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
