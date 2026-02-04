package com.elowen.admin.exception;

/**
 * Exception thrown when an attribute is not found or does not belong to the client
 */
public class AttributeNotFoundException extends RuntimeException {
    
    private final Integer attributeId;
    private final Integer clientId;
    
    public AttributeNotFoundException(Integer attributeId, Integer clientId) {
        super(String.format("Attribute with ID %d not found for client %d", attributeId, clientId));
        this.attributeId = attributeId;
        this.clientId = clientId;
    }
    
    public AttributeNotFoundException(String message) {
        super(message);
        this.attributeId = null;
        this.clientId = null;
    }
    
    public Integer getAttributeId() {
        return attributeId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
