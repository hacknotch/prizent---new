package com.elowen.admin.exception;

/**
 * Exception thrown when invalid custom field value is provided
 */
public class InvalidCustomFieldValueException extends RuntimeException {
    
    public InvalidCustomFieldValueException(String message) {
        super(message);
    }
}
