package com.elowen.admin.exception;

/**
 * Exception thrown when a duplicate custom field name is detected for a client and module
 */
public class DuplicateCustomFieldException extends RuntimeException {
    
    private final String fieldName;
    private final String module;
    private final Integer clientId;
    
    public DuplicateCustomFieldException(String fieldName, String module, Integer clientId) {
        super(String.format("Custom field '%s' already exists for module '%s' and client %d", 
            fieldName, module, clientId));
        this.fieldName = fieldName;
        this.module = module;
        this.clientId = clientId;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public String getModule() {
        return module;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
