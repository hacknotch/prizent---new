package com.elowen.admin.dto;

import com.elowen.admin.entity.CustomFieldValue;

import java.time.LocalDateTime;

/**
 * DTO for Custom Field Value response
 */
public class CustomFieldValueResponse {
    
    private Long id;
    private Long customFieldId;
    private Integer clientId;
    private Long moduleId;
    private String module;
    private String value;
    private Long updatedBy;
    private LocalDateTime createDateTime;
    
    // Constructors
    public CustomFieldValueResponse() {}
    
    public CustomFieldValueResponse(Long id, Long customFieldId, Integer clientId, Long moduleId, String module,
                                    String value, Long updatedBy, LocalDateTime createDateTime) {
        this.id = id;
        this.customFieldId = customFieldId;
        this.clientId = clientId;
        this.moduleId = moduleId;
        this.module = module;
        this.value = value;
        this.updatedBy = updatedBy;
        this.createDateTime = createDateTime;
    }
    
    /**
     * Convert CustomFieldValue entity to CustomFieldValueResponse DTO
     */
    public static CustomFieldValueResponse fromEntity(CustomFieldValue fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        
        return new CustomFieldValueResponse(
            fieldValue.getId(),
            fieldValue.getCustomFieldId(),
            fieldValue.getClientId(),
            fieldValue.getModuleId(),
            fieldValue.getModule(),
            fieldValue.getValue(),
            fieldValue.getUpdatedBy(),
            fieldValue.getCreateDateTime()
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomFieldId() {
        return customFieldId;
    }
    
    public void setCustomFieldId(Long customFieldId) {
        this.customFieldId = customFieldId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
    
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
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
}
