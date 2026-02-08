package com.elowen.admin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_custom_fields_configuration")
public class CustomFieldConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 1)
    private String module; // p = product, m = marketplace, b = brand, c = category
    
    @Column(name = "field_type", nullable = false)
    private String fieldType; // text, numeric, dropdown, date, file
    
    @Column(nullable = false)
    private Boolean required = false;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @CreationTimestamp
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;
    
    @UpdateTimestamp
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

    // Constructors
    public CustomFieldConfiguration() {}

    public CustomFieldConfiguration(Integer clientId, String name, String module, 
                                     String fieldType, Boolean required, Boolean enabled) {
        this.clientId = clientId;
        this.name = name;
        this.module = module;
        this.fieldType = fieldType;
        this.required = required != null ? required : false;
        this.enabled = enabled != null ? enabled : true;
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
