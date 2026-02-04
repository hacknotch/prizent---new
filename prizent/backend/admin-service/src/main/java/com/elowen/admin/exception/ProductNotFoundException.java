package com.elowen.admin.exception;

/**
 * Exception thrown when a product is not found or does not belong to the client
 */
public class ProductNotFoundException extends RuntimeException {
    
    private final Integer productId;
    private final Integer clientId;
    
    public ProductNotFoundException(Integer productId, Integer clientId) {
        super(String.format("Product with ID %d not found for client %d", productId, clientId));
        this.productId = productId;
        this.clientId = clientId;
    }
    
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
        this.clientId = null;
    }
    
    public Integer getProductId() {
        return productId;
    }
    
    public Integer getClientId() {
        return clientId;
    }
}
