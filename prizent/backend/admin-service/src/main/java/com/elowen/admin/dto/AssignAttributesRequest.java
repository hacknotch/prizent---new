package com.elowen.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for assigning/replacing attributes to a category
 */
public class AssignAttributesRequest {
    
    @NotEmpty(message = "Attribute IDs list cannot be empty")
    private List<Integer> attributeIds;
    
    // Constructors
    public AssignAttributesRequest() {}
    
    public AssignAttributesRequest(List<Integer> attributeIds) {
        this.attributeIds = attributeIds;
    }
    
    // Getters and Setters
    public List<Integer> getAttributeIds() {
        return attributeIds;
    }
    
    public void setAttributeIds(List<Integer> attributeIds) {
        this.attributeIds = attributeIds;
    }
    
    @Override
    public String toString() {
        return "AssignAttributesRequest{" +
                "attributeIds=" + attributeIds +
                '}';
    }
}
