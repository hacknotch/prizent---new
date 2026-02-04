package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CategoryAttribute entity - mapping table for category-attribute relationships.
 * 
 * This represents the many-to-many relationship between categories and attributes.
 * When an attribute is assigned to a category, products under that category
 * can have values for that attribute.
 * 
 * Key Design:
 * - Uses INTEGER for all IDs (no UUIDs)
 * - client_id included for tenant safety and consistency checks
 * - Unique constraint on (client_id, category_id, attribute_id)
 * - All three entities (client, category, attribute) must belong to same client
 */
@Entity
@Table(
    name = "p_category_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_category_attributes_client_cat_attr",
            columnNames = {"client_id", "category_id", "attribute_id"}
        )
    },
    indexes = {
        @Index(name = "idx_category_attributes_client_id", columnList = "client_id"),
        @Index(name = "idx_category_attributes_category_id", columnList = "category_id"),
        @Index(name = "idx_category_attributes_attribute_id", columnList = "attribute_id")
    }
)
public class CategoryAttribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;
    
    /**
     * Client ID - IMMUTABLE after creation
     * Ensures all relationships are within same tenant
     */
    @NotNull
    @Column(name = "client_id", updatable = false, nullable = false)
    private Integer clientId;
    
    @NotNull
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @NotNull
    @Column(name = "attribute_id", nullable = false)
    private Integer attributeId;
    
    @CreationTimestamp
    @Column(name = "create_date_time", updatable = false, nullable = false)
    private LocalDateTime createDateTime;
    
    @UpdateTimestamp
    @Column(name = "update_date_time", nullable = false)
    private LocalDateTime updateDateTime;
    
    // Constructors
    public CategoryAttribute() {}
    
    public CategoryAttribute(Integer clientId, Integer categoryId, Integer attributeId) {
        this.clientId = clientId;
        this.categoryId = categoryId;
        this.attributeId = attributeId;
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
    
    public Integer getAttributeId() {
        return attributeId;
    }
    
    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
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
        return "CategoryAttribute{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", categoryId=" + categoryId +
                ", attributeId=" + attributeId +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
