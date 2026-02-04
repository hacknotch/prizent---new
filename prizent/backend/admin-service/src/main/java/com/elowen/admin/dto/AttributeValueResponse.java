package com.elowen.admin.dto;

import com.elowen.admin.entity.AttributeValue;

/**
 * DTO for AttributeValue response
 * Does NOT expose clientId, createDateTime, updateDateTime per requirements
 */
public class AttributeValueResponse {
    
    private Integer id;
    private Integer attributeId;
    private String value;
    private Boolean enabled;
    
    // Constructors
    public AttributeValueResponse() {}
    
    public AttributeValueResponse(Integer id, Integer attributeId, String value, Boolean enabled) {
        this.id = id;
        this.attributeId = attributeId;
        this.value = value;
        this.enabled = enabled;
    }
    
    /**
     * Convert AttributeValue entity to AttributeValueResponse DTO
     */
    public static AttributeValueResponse fromEntity(AttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        
        return new AttributeValueResponse(
            attributeValue.getId(),
            attributeValue.getAttributeId(),
            attributeValue.getValue(),
            attributeValue.getEnabled()
        );
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
    
    @Override
    public String toString() {
        return "AttributeValueResponse{" +
                "id=" + id +
                ", attributeId=" + attributeId +
                ", value='" + value + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
