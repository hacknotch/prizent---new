package com.elowen.admin.dto;

import com.elowen.admin.entity.Attribute;

import java.time.LocalDateTime;

/**
 * DTO for Attribute response
 */
public class AttributeResponse {
    
    private Integer id;
    private Integer clientId;
    private String name;
    private Boolean enabled;
    private LocalDateTime createDateTime;
    private LocalDateTime updateDateTime;
    
    // Constructors
    public AttributeResponse() {}
    
    public AttributeResponse(Integer id, Integer clientId, String name, Boolean enabled,
                           LocalDateTime createDateTime, LocalDateTime updateDateTime) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.enabled = enabled;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
    }
    
    /**
     * Convert Attribute entity to AttributeResponse DTO
     */
    public static AttributeResponse fromEntity(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        
        return new AttributeResponse(
            attribute.getId(),
            attribute.getClientId(),
            attribute.getName(),
            attribute.getEnabled(),
            attribute.getCreateDateTime(),
            attribute.getUpdateDateTime()
        );
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
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
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
    
    @Override
    public String toString() {
        return "AttributeResponse{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
