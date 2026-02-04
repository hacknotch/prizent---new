package com.elowen.admin.repository;

import com.elowen.admin.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CategoryAttribute mapping entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Integer> {
    
    /**
     * Find all attribute mappings for a specific category (tenant-safe)
     */
    List<CategoryAttribute> findAllByCategoryIdAndClientId(Integer categoryId, Integer clientId);
    
    /**
     * Find all category mappings for a specific attribute (tenant-safe)
     */
    List<CategoryAttribute> findAllByAttributeIdAndClientId(Integer attributeId, Integer clientId);
    
    /**
     * Check if specific category-attribute mapping exists
     */
    boolean existsByCategoryIdAndAttributeIdAndClientId(Integer categoryId, Integer attributeId, Integer clientId);
    
    /**
     * Delete all attribute mappings for a specific category (tenant-safe)
     * Used when replacing category attributes
     */
    @Modifying
    @Query("DELETE FROM CategoryAttribute ca WHERE ca.categoryId = :categoryId AND ca.clientId = :clientId")
    void deleteByCategoryIdAndClientId(@Param("categoryId") Integer categoryId, @Param("clientId") Integer clientId);
    
    /**
     * Delete specific category-attribute mapping (tenant-safe)
     */
    @Modifying
    @Query("DELETE FROM CategoryAttribute ca WHERE ca.categoryId = :categoryId AND ca.attributeId = :attributeId AND ca.clientId = :clientId")
    void deleteByCategoryIdAndAttributeIdAndClientId(
        @Param("categoryId") Integer categoryId,
        @Param("attributeId") Integer attributeId,
        @Param("clientId") Integer clientId
    );
    
    /**
     * Count attribute assignments for a category
     */
    long countByCategoryIdAndClientId(Integer categoryId, Integer clientId);
}
