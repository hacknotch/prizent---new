package com.elowen.admin.exception;

/**
 * Exception thrown when a category is not found or does not belong to the client
 */
public class CategoryNotFoundException extends RuntimeException {
    
    private final Integer categoryId;
    private final Integer clientId;
    
    public CategoryNotFoundException(Integer categoryId, Integer clientId) {
        super(String.format("Category with ID %d not found for client %d", categoryId, clientId));
        this.categoryId = categoryId;
        this.clientId = clientId;
    }
    
    public CategoryNotFoundException(String message) {
        super(message);
        this.categoryId = null;
        this.clientId = null;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
