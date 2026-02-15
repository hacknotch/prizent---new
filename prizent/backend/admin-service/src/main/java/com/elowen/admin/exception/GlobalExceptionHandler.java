package com.elowen.admin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(CustomFieldNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomFieldNotFound(CustomFieldNotFoundException ex) {
        log.error("Custom field not found: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "CUSTOM_FIELD_NOT_FOUND");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(DuplicateCustomFieldException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCustomField(DuplicateCustomFieldException ex) {
        log.error("Duplicate custom field: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "DUPLICATE_CUSTOM_FIELD");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(InvalidCustomFieldValueException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCustomFieldValue(InvalidCustomFieldValueException ex) {
        log.error("Invalid custom field value: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "INVALID_CUSTOM_FIELD_VALUE");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.error("Category not found: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "CATEGORY_NOT_FOUND");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        log.error("Product not found: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "PRODUCT_NOT_FOUND");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(InvalidCategoryHierarchyException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCategoryHierarchy(InvalidCategoryHierarchyException ex) {
        log.error("Invalid category hierarchy: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "INVALID_CATEGORY_HIERARCHY");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MarketplaceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMarketplaceNotFound(MarketplaceNotFoundException ex) {
        log.error("Marketplace not found: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "MARKETPLACE_NOT_FOUND");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(DuplicateMarketplaceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateMarketplace(DuplicateMarketplaceException ex) {
        log.error("Duplicate marketplace: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "DUPLICATE_MARKETPLACE");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "INVALID_ARGUMENT");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data constraint violation occurred";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();
            if (causeMessage.contains("duplicate") || causeMessage.contains("unique")) {
                message = "Duplicate entry detected";
            } else if (causeMessage.contains("foreign key")) {
                message = "Referenced entity not found";
            } else if (causeMessage.contains("not null")) {
                message = "Required field cannot be empty";
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "DATA_CONSTRAINT_VIOLATION");
        response.put("message", message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "VALIDATION_FAILED");
        response.put("message", "Invalid input data");
        response.put("fieldErrors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
