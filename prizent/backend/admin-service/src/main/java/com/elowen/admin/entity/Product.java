package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Product entity representing client-owned products.
 * 
 * Key Design:
 * - Uses INTEGER for all IDs (no UUIDs)
 * - client_id is immutable and extracted from UserPrincipal
 * - category_id links to p_categories - products belong to ONE category
 * - Unique constraint on (client_id, category_id, name)
 * - Soft delete pattern using 'enabled' field
 * 
 * CRITICAL RULE:
 * Products can ONLY use attributes and values assigned to their category.
 * This is enforced in the service layer during create/update operations.
 */
@Entity
@Table(
    name = "p_products",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_products_client_category_name",
            columnNames = {"client_id", "category_id", "name"}
        )
    },
    indexes = {
        @Index(name = "idx_products_client_id", columnList = "client_id"),
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_enabled", columnList = "enabled")
    }
)
public class Product {
    
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
     * Category ID - links to p_categories
     * Can be updated but must pass same validations as create
     */
    @NotNull
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
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
    public Product() {}
    
    public Product(Integer clientId, Integer categoryId, String name, String description) {
        this.clientId = clientId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
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
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
        return "Product{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
