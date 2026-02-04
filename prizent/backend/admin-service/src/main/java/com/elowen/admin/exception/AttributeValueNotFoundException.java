package com.elowen.admin.exception;

/**
 * Exception thrown when an attribute value is not found or does not belong to the client
 */
public class AttributeValueNotFoundException extends RuntimeException {
    
    private final Integer attributeValueId;
    private final Integer clientId;
    
    public AttributeValueNotFoundException(Integer attributeValueId, Integer clientId) {
        super(String.format("Attribute value with ID %d not found for client %d", attributeValueId, clientId));
        this.attributeValueId = attributeValueId;
        this.clientId = clientId;
    }
    
    public AttributeValueNotFoundException(String message) {
        super(message);
        this.attributeValueId = null;
        this.clientId = null;
    }
    
    public Integer getAttributeValueId() {
        return attributeValueId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
