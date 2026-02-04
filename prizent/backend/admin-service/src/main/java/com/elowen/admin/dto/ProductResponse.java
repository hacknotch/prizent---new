package com.elowen.admin.dto;

import com.elowen.admin.entity.Product;

/**
 * DTO for Product response
 * Does NOT expose clientId, createDateTime, updateDateTime per requirements
 */
public class ProductResponse {
    
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Boolean enabled;
    
    // Constructors
    public ProductResponse() {}
    
    public ProductResponse(Integer id, Integer categoryId, String categoryName, String name, 
                          String description, Boolean enabled) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }
    
    /**
     * Convert Product entity to ProductResponse DTO
     */
    public static ProductResponse fromEntity(Product product, String categoryName) {
        if (product == null) {
            return null;
        }
        
        return new ProductResponse(
            product.getId(),
            product.getCategoryId(),
            categoryName,
            product.getName(),
            product.getDescription(),
            product.getEnabled()
        );
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public String toString() {
        return "ProductResponse{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
