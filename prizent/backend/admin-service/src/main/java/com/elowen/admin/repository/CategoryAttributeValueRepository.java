package com.elowen.admin.repository;

import com.elowen.admin.entity.CategoryAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CategoryAttributeValue mapping entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface CategoryAttributeValueRepository extends JpaRepository<CategoryAttributeValue, Integer> {
    
    /**
     * Find all attribute value mappings for a specific category (tenant-safe)
     */
    List<CategoryAttributeValue> findAllByCategoryIdAndClientId(Integer categoryId, Integer clientId);
    
    /**
     * Find all attribute value mappings for a specific category and attribute
     */
    List<CategoryAttributeValue> findAllByCategoryIdAndAttributeIdAndClientId(
        Integer categoryId, Integer attributeId, Integer clientId);
    
    /**
     * Check if specific category-attribute-value mapping exists
     */
    boolean existsByCategoryIdAndAttributeValueIdAndClientId(
        Integer categoryId, Integer attributeValueId, Integer clientId);
    
    /**
     * Delete all attribute value mappings for a specific category (tenant-safe)
     * Used when replacing category attribute values
     */
    @Modifying
    @Query("DELETE FROM CategoryAttributeValue cav " +
           "WHERE cav.categoryId = :categoryId " +
           "AND cav.clientId = :clientId")
    void deleteByCategoryIdAndClientId(
        @Param("categoryId") Integer categoryId,
        @Param("clientId") Integer clientId
    );
    
    /**
     * Delete specific category-attribute-value mapping (tenant-safe)
     */
    @Modifying
    @Query("DELETE FROM CategoryAttributeValue cav " +
           "WHERE cav.categoryId = :categoryId " +
           "AND cav.attributeValueId = :attributeValueId " +
           "AND cav.clientId = :clientId")
    void deleteByCategoryIdAndAttributeValueIdAndClientId(
        @Param("categoryId") Integer categoryId,
        @Param("attributeValueId") Integer attributeValueId,
        @Param("clientId") Integer clientId
    );
    
    /**
     * Count attribute value assignments for a category
     */
    long countByCategoryIdAndClientId(Integer categoryId, Integer clientId);
    
    /**
     * Get distinct attribute IDs assigned to a category
     */
    @Query("SELECT DISTINCT cav.attributeId FROM CategoryAttributeValue cav " +
           "WHERE cav.categoryId = :categoryId " +
           "AND cav.clientId = :clientId")
    List<Integer> findDistinctAttributeIdsByCategoryIdAndClientId(
        @Param("categoryId") Integer categoryId,
        @Param("clientId") Integer clientId
    );
}
