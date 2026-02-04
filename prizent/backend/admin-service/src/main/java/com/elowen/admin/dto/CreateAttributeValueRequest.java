package com.elowen.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new attribute value
 */
public class CreateAttributeValueRequest {
    
    @NotBlank(message = "Attribute value is required")
    @Size(max = 255, message = "Attribute value must not exceed 255 characters")
    private String value;
    
    // Constructors
    public CreateAttributeValueRequest() {}
    
    public CreateAttributeValueRequest(String value) {
        this.value = value;
    }
    
    // Getters and Setters
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "CreateAttributeValueRequest{" +
                "value='" + value + '\'' +
                '}';
    }
}
