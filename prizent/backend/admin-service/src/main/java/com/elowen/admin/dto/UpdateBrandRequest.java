package com.elowen.admin.dto;

import jakarta.validation.constraints.Size;

public class UpdateBrandRequest {
    
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;
    
    private Boolean enabled;

    // Constructors
    public UpdateBrandRequest() {}

    // Getters and Setters
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
}
