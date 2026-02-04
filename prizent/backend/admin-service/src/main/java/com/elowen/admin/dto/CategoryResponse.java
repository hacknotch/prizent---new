package com.elowen.admin.dto;

import com.elowen.admin.entity.Category;

import java.time.LocalDateTime;

/**
 * DTO for Category response
 * 
 * Returns flat list of categories - UI will build hierarchy
 */
public class CategoryResponse {
    
    private Integer id;
    private Integer clientId;
    private String name;
    private Integer parentCategoryId;
    private Boolean enabled;
    private LocalDateTime createDateTime;
    private LocalDateTime updateDateTime;
    
    // Constructors
    public CategoryResponse() {}
    
    public CategoryResponse(Integer id, Integer clientId, String name, Integer parentCategoryId, 
                          Boolean enabled, LocalDateTime createDateTime, LocalDateTime updateDateTime) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.enabled = enabled;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
    }
    
    /**
     * Convert Category entity to CategoryResponse DTO
     */
    public static CategoryResponse fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        
        return new CategoryResponse(
            category.getId(),
            category.getClientId(),
            category.getName(),
            category.getParentCategoryId(),
            category.getEnabled(),
            category.getCreateDateTime(),
            category.getUpdateDateTime()
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
    
    public Integer getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
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
        return "CategoryResponse{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", name='" + name + '\'' +
                ", parentCategoryId=" + parentCategoryId +
                ", enabled=" + enabled +
                ", createDateTime=" + createDateTime +
                ", updateDateTime=" + updateDateTime +
                '}';
    }
}
