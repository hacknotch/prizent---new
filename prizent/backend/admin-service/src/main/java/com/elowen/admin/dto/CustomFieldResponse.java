package com.elowen.admin.dto;

import com.elowen.admin.entity.CustomFieldConfiguration;

import java.time.LocalDateTime;

/**
 * DTO for Custom Field Configuration response
 */
public class CustomFieldResponse {
    
    private Long id;
    private Integer clientId;
    private String name;
    private String module;
    private String fieldType;
    private Boolean required;
    private Boolean enabled;
    private Long updatedBy;
    private LocalDateTime createDateTime;
    private LocalDateTime updateDateTime;
    
    // Constructors
    public CustomFieldResponse() {}
    
    public CustomFieldResponse(Long id, Integer clientId, String name, String module, 
                               String fieldType, Boolean required, Boolean enabled,
                               Long updatedBy, LocalDateTime createDateTime, LocalDateTime updateDateTime) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.module = module;
        this.fieldType = fieldType;
        this.required = required;
        this.enabled = enabled;
        this.updatedBy = updatedBy;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
    }
    
    /**
     * Convert CustomFieldConfiguration entity to CustomFieldResponse DTO
     */
    public static CustomFieldResponse fromEntity(CustomFieldConfiguration field) {
        if (field == null) {
            return null;
        }
        
        return new CustomFieldResponse(
            field.getId(),
            field.getClientId(),
            field.getName(),
            field.getModule(),
            field.getFieldType(),
            field.getRequired(),
            field.getEnabled(),
            field.getUpdatedBy(),
            field.getCreateDateTime(),
            field.getUpdateDateTime()
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getClientId() {
        return clientId;
    }
    
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    
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
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
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
}
