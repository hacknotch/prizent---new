package com.elowen.admin.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing category
 * 
 * Allows partial updates:
 * - name (optional)
 * - parentCategoryId (optional)
 * 
 * SECURITY NOTE:
 * - client_id cannot be modified (enforced at entity level)
 * - All updates are validated for hierarchy integrity
 */
public class UpdateCategoryRequest {
    
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;
    
    /**
     * Parent category ID - can be updated
     * Setting to null makes it a root category
     * Must pass cycle detection validation
     */
    private Integer parentCategoryId;
    
    // Track which fields are being updated
    private boolean hasName = false;
    private boolean hasParentCategoryId = false;
    
    // Constructors
    public UpdateCategoryRequest() {}
    
    public UpdateCategoryRequest(String name, Integer parentCategoryId) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.hasName = name != null;
        this.hasParentCategoryId = true; // Even null is an update
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.hasName = name != null;
    }
    
    public Integer getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
        this.hasParentCategoryId = true;
    }
    
    public boolean hasName() {
        return hasName;
    }
    
    public boolean hasParentCategoryId() {
        return hasParentCategoryId;
    }
    
    public boolean isEmpty() {
        return !hasName && !hasParentCategoryId;
    }
    
    @Override
    public String toString() {
        return "UpdateCategoryRequest{" +
                "name='" + name + '\'' +
                ", parentCategoryId=" + parentCategoryId +
                ", hasName=" + hasName +
                ", hasParentCategoryId=" + hasParentCategoryId +
                '}';
    }
}
