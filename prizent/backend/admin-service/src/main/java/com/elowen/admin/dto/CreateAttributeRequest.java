package com.elowen.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new attribute
 */
public class CreateAttributeRequest {
    
    @NotBlank(message = "Attribute name is required")
    @Size(max = 255, message = "Attribute name must not exceed 255 characters")
    private String name;
    
    // Constructors
    public CreateAttributeRequest() {}
    
    public CreateAttributeRequest(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "CreateAttributeRequest{" +
                "name='" + name + '\'' +
                '}';
    }
}
