package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Category entity representing client-owned hierarchical categories.
 * 
 * Key Design Decisions:
 * - Uses INTEGER for id and foreign keys (no UUIDs)
 * - Self-referencing via parent_category_id for unlimited depth hierarchy
 * - client_id is immutable and extracted from UserPrincipal
 * - Composite unique constraint on (client_id, name, parent_category_id)
 * - Soft delete pattern using 'enabled' field - no physical deletion
 * - Cycle detection enforced in service layer before save
 * 
 * HIERARCHY RULES:
 * - A category cannot be its own parent
 * - A category cannot be assigned to any of its descendants
 * - Parent category must belong to the same client
 * - Parent category must be enabled
 */
@Entity
@Table(
    name = "p_categories",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_categories_client_name_parent",
            columnNames = {"client_id", "name", "parent_category_id"}
        )
    },
    indexes = {
        @Index(name = "idx_categories_client_id", columnList = "client_id"),
        @Index(name = "idx_categories_parent_id", columnList = "parent_category_id"),
        @Index(name = "idx_categories_enabled", columnList = "enabled")
    }
)
public class Category {
    
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
     * Parent category ID - nullable for root categories
     * Self-referencing foreign key for hierarchy
     */
    @Column(name = "parent_category_id")
    private Integer parentCategoryId;
    
    /**
     * Soft delete flag - true = active, false = disabled/soft-deleted
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
    public Category() {}
    
    public Category(Integer clientId, String name, Integer parentCategoryId) {
        this.clientId = clientId;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
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
    
    /**
     * Client ID setter - should only be used during entity creation
     * In production, this comes from JWT and should be immutable
     */
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
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
        return "Category{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", name='" + name + '\'' +
                ", parentCategoryId=" + parentCategoryId +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
