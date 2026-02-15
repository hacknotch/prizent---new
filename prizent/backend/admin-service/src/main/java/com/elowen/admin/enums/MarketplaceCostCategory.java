package com.elowen.admin.enums;

public enum MarketplaceCostCategory {
    COMMISSION,
    SHIPPING,
    MARKETING;
    
    public static MarketplaceCostCategory fromString(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Cost category cannot be null or empty");
        }
        
        try {
            return MarketplaceCostCategory.valueOf(category.toUpperCase().trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cost category: " + category + 
                ". Valid values are: COMMISSION, SHIPPING, MARKETING");
        }
    }
}