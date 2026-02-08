package com.elowen.admin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_custom_fields_values")
public class CustomFieldValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "custom_field_id", nullable = false)
    private Long customFieldId;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Column(name = "module_id", nullable = false)
    private Long moduleId;
    
    @Column(nullable = false, length = 1)
    private String module; // p = product, m = marketplace, b = brand, c = category
    
    @Column(columnDefinition = "TEXT")
    private String value;
    
    @CreationTimestamp
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_by")
    private Long updatedBy;

    // Constructors
    public CustomFieldValue() {}

    public CustomFieldValue(Long customFieldId, Integer clientId, Long moduleId, String module, String value, Long updatedBy) {
        this.customFieldId = customFieldId;
        this.clientId = clientId;
        this.moduleId = moduleId;
        this.module = module;
        this.value = value;
        this.updatedBy = updatedBy;
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

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
