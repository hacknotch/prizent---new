package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CategoryAttributeValue entity - mapping table for category-attribute-value relationships.
 * 
 * This represents which specific attribute values are allowed/assigned to a category.
 * For example:
 * - Category "T-Shirts" → Attribute "Size" → Values: S, M, L, XL
 * - Category "Formal Shirts" → Attribute "Size" → Values: 38, 40, 42, 44
 * 
 * Key Design:
 * - Uses INTEGER for all IDs (no UUIDs)
 * - client_id included for tenant safety and consistency checks
 * - Unique constraint on (client_id, category_id, attribute_value_id)
 * - All entities (client, category, attribute, value) must belong to same client
 * - Attribute must be assigned to category before values can be assigned
 */
@Entity
@Table(
    name = "p_category_attribute_values",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_category_attr_values_client_cat_val",
            columnNames = {"client_id", "category_id", "attribute_value_id"}
        )
    },
    indexes = {
        @Index(name = "idx_category_attr_values_client_id", columnList = "client_id"),
        @Index(name = "idx_category_attr_values_category_id", columnList = "category_id"),
        @Index(name = "idx_category_attr_values_attribute_id", columnList = "attribute_id"),
        @Index(name = "idx_category_attr_values_value_id", columnList = "attribute_value_id")
    }
)
public class CategoryAttributeValue {
    
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
    
    @NotNull
    @Column(name = "attribute_value_id", nullable = false)
    private Integer attributeValueId;
    
    @CreationTimestamp
    @Column(name = "create_date_time", updatable = false, nullable = false)
    private LocalDateTime createDateTime;
    
    @UpdateTimestamp
    @Column(name = "update_date_time", nullable = false)
    private LocalDateTime updateDateTime;
    
    // Constructors
    public CategoryAttributeValue() {}
    
    public CategoryAttributeValue(Integer clientId, Integer categoryId, Integer attributeId, Integer attributeValueId) {
        this.clientId = clientId;
        this.categoryId = categoryId;
        this.attributeId = attributeId;
        this.attributeValueId = attributeValueId;
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
    
    public Integer getAttributeValueId() {
        return attributeValueId;
    }
    
    public void setAttributeValueId(Integer attributeValueId) {
        this.attributeValueId = attributeValueId;
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
        return "CategoryAttributeValue{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", categoryId=" + categoryId +
                ", attributeId=" + attributeId +
                ", attributeValueId=" + attributeValueId +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
