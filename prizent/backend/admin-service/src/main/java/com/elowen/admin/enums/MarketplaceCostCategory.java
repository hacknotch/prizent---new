package com.elowen.admin.enums;

public enum MarketplaceCostCategory {
    COMMISSION,
    SHIPPING,
    MARKETING,
    WEIGHT_SHIPPING,
    WEIGHT_SHIPPING_LOCAL,
    WEIGHT_SHIPPING_ZONAL,
    WEIGHT_SHIPPING_NATIONAL,
    SHIPPING_PERCENTAGE_LOCAL,
    SHIPPING_PERCENTAGE_ZONAL,
    SHIPPING_PERCENTAGE_NATIONAL,
    FIXED_FEE,
    REVERSE_SHIPPING,
    REVERSE_WEIGHT_SHIPPING,
    REVERSE_SHIPPING_LOCAL,
    REVERSE_SHIPPING_ZONAL,
    REVERSE_SHIPPING_NATIONAL,
    COLLECTION_FEE_PREPAID,
    COLLECTION_FEE_POSTPAID,
    ROYALTY,
    PICK_AND_PACK;
    
    public static MarketplaceCostCategory fromString(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Cost category cannot be null or empty");
        }
        
        try {
            return MarketplaceCostCategory.valueOf(category.toUpperCase().trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cost category: " + category);
        }
    }
}