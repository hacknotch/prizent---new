package com.elowen.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new custom field configuration
 * 
 * SECURITY NOTE:
 * - client_id is extracted from authenticated UserPrincipal, NOT from request
 */
public class CreateCustomFieldRequest {
    
    @NotBlank(message = "Field name is required")
    @Size(max = 255, message = "Field name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Module is required")
    @Pattern(regexp = "^[pmbc]$", message = "Module must be one of: p (product), m (marketplace), b (brand), c (category)")
    private String module;
    
    @NotBlank(message = "Field type is required")
    @Pattern(regexp = "^(text|numeric|dropdown|date|file)$", message = "Field type must be one of: text, numeric, dropdown, date, file")
    private String fieldType;
    
    @NotNull(message = "Required flag must be specified")
    private Boolean required;
    
    private Boolean enabled = true;
    
    // Constructors
    public CreateCustomFieldRequest() {}
    
    public CreateCustomFieldRequest(String name, String module, String fieldType, Boolean required, Boolean enabled) {
        this.name = name;
        this.module = module;
        this.fieldType = fieldType;
        this.required = required;
        this.enabled = enabled != null ? enabled : true;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
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
