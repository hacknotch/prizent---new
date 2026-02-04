package com.elowen.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * DTO for creating a new product
 */
public class CreateProductRequest {
    
    @NotNull(message = "Category ID is required")
    private Integer categoryId;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Product description must not exceed 5000 characters")
    private String description;
    
    /**
     * Map of attribute ID to attribute value ID
     * Example: {1: 10, 2: 15} means Attribute 1 → Value 10, Attribute 2 → Value 15
     */
    private Map<Integer, Integer> attributes;
    
    // Constructors
    public CreateProductRequest() {}
    
    public CreateProductRequest(Integer categoryId, String name, String description, Map<Integer, Integer> attributes) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.attributes = attributes;
    }
    
    // Getters and Setters
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
    
    public Map<Integer, Integer> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<Integer, Integer> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String toString() {
        return "CreateProductRequest{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
