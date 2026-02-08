package com.elowen.admin.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing custom field configuration
 * All fields are optional - only non-null fields will be updated
 */
public class UpdateCustomFieldRequest {
    
    @Size(max = 255, message = "Field name must not exceed 255 characters")
    private String name;
    
    @Pattern(regexp = "^(text|numeric|dropdown|date|file)$", message = "Field type must be one of: text, numeric, dropdown, date, file")
    private String fieldType;
    
    private Boolean required;
    
    private Boolean enabled;
    
    // Constructors
    public UpdateCustomFieldRequest() {}
    
    public UpdateCustomFieldRequest(String name, String fieldType, Boolean required, Boolean enabled) {
        this.name = name;
        this.fieldType = fieldType;
        this.required = required;
        this.enabled = enabled;
    }
    
    // Helper method to check if request is empty
    public boolean isEmpty() {
        return name == null && fieldType == null && required == null && enabled == null;
    }
    
    public boolean hasName() {
        return name != null;
    }
    
    public boolean hasFieldType() {
        return fieldType != null;
    }
    
    public boolean hasRequired() {
        return required != null;
    }
    
    public boolean hasEnabled() {
        return enabled != null;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFieldType() {
        return fieldType;
    }
    
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
