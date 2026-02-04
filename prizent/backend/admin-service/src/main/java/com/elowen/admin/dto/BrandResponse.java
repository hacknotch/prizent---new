package com.elowen.admin.dto;

import com.elowen.admin.entity.Brand;

import java.time.LocalDateTime;

public class BrandResponse {
    
    private Long id;
    private Integer clientId;
    private String name;
    private String description;
    private String logoUrl;
    private Boolean enabled;
    private LocalDateTime createDateTime;
    private LocalDateTime updateDateTime;
    
    // Constructors
    public BrandResponse() {}

    public BrandResponse(Brand brand) {
        this.id = brand.getId();
        this.clientId = brand.getClientId();
        this.name = brand.getName();
        this.description = brand.getDescription();
        this.logoUrl = brand.getLogoUrl();
        this.enabled = brand.getEnabled();
        this.createDateTime = brand.getCreateDateTime();
        this.updateDateTime = brand.getUpdateDateTime();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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
}
