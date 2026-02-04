package com.elowen.admin.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoryTreeNode {
    
    private Integer id;
    private String name;
    private Integer parentCategoryId;
    private Boolean enabled;
    private LocalDateTime createDateTime;
    private LocalDateTime updateDateTime;
    private List<CategoryTreeNode> children;
    
    public CategoryTreeNode() {
        this.children = new ArrayList<>();
    }
    
    public CategoryTreeNode(Integer id, String name, Integer parentCategoryId, Boolean enabled, 
                           LocalDateTime createDateTime, LocalDateTime updateDateTime) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.enabled = enabled;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
        this.children = new ArrayList<>();
    }
    
    public void addChild(CategoryTreeNode child) {
        this.children.add(child);
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
    
    public List<CategoryTreeNode> getChildren() {
        return children;
    }
    
    public void setChildren(List<CategoryTreeNode> children) {
        this.children = children;
    }
}
