package com.elowen.admin.dto;

/**
 * DTO for product attribute assignment in response
 * Does NOT expose clientId, timestamps, or join table IDs
 */
public class ProductAttributeResponse {
    
    private Integer attributeId;
    private String attributeName;
    private Integer attributeValueId;
    private String attributeValue;
    
    // Constructors
    public ProductAttributeResponse() {}
    
    public ProductAttributeResponse(Integer attributeId, String attributeName, 
                                    Integer attributeValueId, String attributeValue) {
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.attributeValueId = attributeValueId;
        this.attributeValue = attributeValue;
    }
    
    // Getters and Setters
    public Integer getAttributeId() {
        return attributeId;
    }
    
    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    
    public Integer getAttributeValueId() {
        return attributeValueId;
    }
    
    public void setAttributeValueId(Integer attributeValueId) {
        this.attributeValueId = attributeValueId;
    }
    
    public String getAttributeValue() {
        return attributeValue;
    }
    
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
    
    @Override
    public String toString() {
        return "ProductAttributeResponse{" +
                "attributeId=" + attributeId +
                ", attributeName='" + attributeName + '\'' +
                ", attributeValueId=" + attributeValueId +
                ", attributeValue='" + attributeValue + '\'' +
                '}';
    }
}
