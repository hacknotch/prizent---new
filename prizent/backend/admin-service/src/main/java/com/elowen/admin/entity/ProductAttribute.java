package com.elowen.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ProductAttribute entity - stores selected attribute values for a product.
 * 
 * This represents which attribute values are assigned to a product.
 * For example, a T-shirt product might have:
 * - Attribute: Size → Value: M
 * - Attribute: Color → Value: Blue
 * - Attribute: Fabric → Value: Cotton
 * 
 * Key Design:
 * - Uses INTEGER for all IDs (no UUIDs)
 * - client_id included for tenant safety
 * - Unique constraint on (product_id, attribute_id) - one value per attribute
 * - All entities must belong to same client
 * 
 * CRITICAL RULE:
 * The attribute and value MUST be assigned to the product's category.
 * This is validated in the service layer.
 */
@Entity
@Table(
    name = "p_product_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_product_attributes_product_attr",
            columnNames = {"product_id", "attribute_id"}
        )
    },
    indexes = {
        @Index(name = "idx_product_attributes_client_id", columnList = "client_id"),
        @Index(name = "idx_product_attributes_product_id", columnList = "product_id"),
        @Index(name = "idx_product_attributes_attribute_id", columnList = "attribute_id"),
        @Index(name = "idx_product_attributes_value_id", columnList = "attribute_value_id")
    }
)
public class ProductAttribute {
    
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
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
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
    public ProductAttribute() {}
    
    public ProductAttribute(Integer clientId, Integer productId, Integer attributeId, Integer attributeValueId) {
        this.clientId = clientId;
        this.productId = productId;
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
    
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
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
        return "ProductAttribute{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", productId=" + productId +
                ", attributeId=" + attributeId +
                ", attributeValueId=" + attributeValueId +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
