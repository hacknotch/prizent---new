package com.elowen.admin.exception;

/**
 * Exception thrown when a custom field configuration is not found or does not belong to the client
 */
public class CustomFieldNotFoundException extends RuntimeException {
    
    private final Long customFieldId;
    private final Integer clientId;
    
    public CustomFieldNotFoundException(Long customFieldId, Integer clientId) {
        super(String.format("Custom field with ID %d not found for client %d", customFieldId, clientId));
        this.customFieldId = customFieldId;
        this.clientId = clientId;
    }
    
    public CustomFieldNotFoundException(String message) {
        super(message);
        this.customFieldId = null;
        this.clientId = null;
    }
    
    public Long getCustomFieldId() {
        return customFieldId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
