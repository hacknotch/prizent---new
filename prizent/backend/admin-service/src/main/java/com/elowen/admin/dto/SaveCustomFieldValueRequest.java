package com.elowen.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for saving custom field values
 * 
 * SECURITY NOTE:
 * - client_id is extracted from authenticated UserPrincipal, NOT from request
 */
public class SaveCustomFieldValueRequest {
    
    @NotNull(message = "Custom field ID is required")
    private Long customFieldId;
    
    @NotNull(message = "Module ID is required")
    private Long moduleId;
    
    @NotBlank(message = "Module is required")
    @Pattern(regexp = "^[pmbc]$", message = "Module must be one of: p (product), m (marketplace), b (brand), c (category)")
    private String module;
    
    private String value;
    
    // Constructors
    public SaveCustomFieldValueRequest() {}
    
    public SaveCustomFieldValueRequest(Long customFieldId, Long moduleId, String module, String value) {
        this.customFieldId = customFieldId;
        this.moduleId = moduleId;
        this.module = module;
        this.value = value;
    }
    
    // Getters and Setters
    public Long getCustomFieldId() {
        return customFieldId;
    }
    
    public void setCustomFieldId(Long customFieldId) {
        this.customFieldId = customFieldId;
    }
    
    public Long getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
