package com.elowen.admin.exception;

public class DuplicateMarketplaceException extends RuntimeException {
    
    public DuplicateMarketplaceException(String message) {
        super(message);
    }
    
    public DuplicateMarketplaceException(String message, Throwable cause) {
        super(message, cause);
    }
}