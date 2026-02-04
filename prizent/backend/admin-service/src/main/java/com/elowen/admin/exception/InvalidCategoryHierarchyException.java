package com.elowen.admin.exception;

/**
 * Exception thrown when category hierarchy validation fails
 * 
 * Scenarios:
 * - Attempting to set self as parent
 * - Attempting to set descendant as parent (creates cycle)
 * - Parent category is disabled
 * - Parent category does not belong to same client
 */
public class InvalidCategoryHierarchyException extends RuntimeException {
    
    public InvalidCategoryHierarchyException(String message) {
        super(message);
    }
    
    public InvalidCategoryHierarchyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Create exception for self-parenting attempt
     */
    public static InvalidCategoryHierarchyException selfParent(Integer categoryId) {
        return new InvalidCategoryHierarchyException(
            String.format("Category %d cannot be its own parent", categoryId)
        );
    }
    
    /**
     * Create exception for cycle detection
     */
    public static InvalidCategoryHierarchyException cycleDetected(Integer categoryId, Integer parentId) {
        return new InvalidCategoryHierarchyException(
            String.format("Cannot set category %d as parent of category %d - this would create a cycle in the hierarchy", 
                         parentId, categoryId)
        );
    }
    
    /**
     * Create exception for disabled parent
     */
    public static InvalidCategoryHierarchyException disabledParent(Integer parentId) {
        return new InvalidCategoryHierarchyException(
            String.format("Cannot assign disabled category %d as parent", parentId)
        );
    }
    
    /**
     * Create exception for parent from different client
     */
    public static InvalidCategoryHierarchyException differentClient(Integer parentId) {
        return new InvalidCategoryHierarchyException(
            String.format("Parent category %d does not belong to the same client", parentId)
        );
    }
}
