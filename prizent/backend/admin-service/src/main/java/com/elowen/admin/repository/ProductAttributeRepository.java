package com.elowen.admin.repository;

import com.elowen.admin.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ProductAttribute mapping entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Integer> {
    
    /**
     * Find all attribute mappings for a specific product (tenant-safe)
     */
    List<ProductAttribute> findAllByProductIdAndClientId(Integer productId, Integer clientId);
    
    /**
     * Check if specific product-attribute mapping exists
     */
    boolean existsByProductIdAndAttributeIdAndClientId(
        Integer productId, Integer attributeId, Integer clientId);
    
    /**
     * Delete all attribute mappings for a specific product (tenant-safe)
     * Used when replacing product attributes
     */
    @Modifying
    @Query("DELETE FROM ProductAttribute pa " +
           "WHERE pa.productId = :productId " +
           "AND pa.clientId = :clientId")
    void deleteByProductIdAndClientId(
        @Param("productId") Integer productId,
        @Param("clientId") Integer clientId
    );
    
    /**
     * Count attribute assignments for a product
     */
    long countByProductIdAndClientId(Integer productId, Integer clientId);
}
