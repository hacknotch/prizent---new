package com.elowen.admin.enums;

public enum CostValueType {
    A("amount"),    // 'a' for amount
    P("percentage"); // 'p' for percentage
    
    private final String description;
    
    CostValueType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CostValueType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Cost value type cannot be null or empty");
        }
        
        String normalizedType = type.toUpperCase().trim();
        
        try {
            return CostValueType.valueOf(normalizedType);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cost value type: " + type + 
                ". Valid values are: A (amount), P (percentage)");
        }
    }
    
    public static CostValueType fromChar(char type) {
        return fromString(String.valueOf(type));
    }
    
    public char toChar() {
        return this.name().charAt(0);
    }
}