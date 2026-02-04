package com.elowen.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for assigning/replacing attribute values to a category
 */
public class AssignAttributeValuesRequest {
    
    @NotEmpty(message = "Attribute value IDs list cannot be empty")
    private List<Integer> attributeValueIds;
    
    // Constructors
    public AssignAttributeValuesRequest() {}
    
    public AssignAttributeValuesRequest(List<Integer> attributeValueIds) {
        this.attributeValueIds = attributeValueIds;
    }
    
    // Getters and Setters
    public List<Integer> getAttributeValueIds() {
        return attributeValueIds;
    }
    
    public void setAttributeValueIds(List<Integer> attributeValueIds) {
        this.attributeValueIds = attributeValueIds;
    }
    
    @Override
    public String toString() {
        return "AssignAttributeValuesRequest{" +
                "attributeValueIds=" + attributeValueIds +
                '}';
    }
}
