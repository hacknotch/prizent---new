package com.elowen.admin.repository;

import com.elowen.admin.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AttributeValue entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - NO findById() methods without tenant isolation
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Integer> {
    
    /**
     * Find attribute value by ID within client's tenant boundary
     * SECURITY: Prevents cross-tenant data access
     */
    Optional<AttributeValue> findByIdAndClientId(Integer id, Integer clientId);
    
    /**
     * Find all values for a specific attribute (tenant-safe)
     */
    List<AttributeValue> findAllByAttributeIdAndClientIdOrderByValueAsc(Integer attributeId, Integer clientId);
    
    /**
     * Find only enabled values for a specific attribute
     */
    List<AttributeValue> findAllByAttributeIdAndClientIdAndEnabledTrueOrderByValueAsc(
        Integer attributeId, Integer clientId);
    
    /**
     * Check if value exists for attribute (case-insensitive)
     */
    @Query("SELECT CASE WHEN COUNT(av) > 0 THEN true ELSE false END FROM AttributeValue av " +
           "WHERE av.clientId = :clientId " +
           "AND av.attributeId = :attributeId " +
           "AND LOWER(av.value) = LOWER(:value)")
    boolean existsByClientIdAndAttributeIdAndValueIgnoreCase(
        @Param("clientId") Integer clientId,
        @Param("attributeId") Integer attributeId,
        @Param("value") String value
    );
    
    /**
     * Check if value exists for attribute excluding specific value ID (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(av) > 0 THEN true ELSE false END FROM AttributeValue av " +
           "WHERE av.clientId = :clientId " +
           "AND av.attributeId = :attributeId " +
           "AND LOWER(av.value) = LOWER(:value) " +
           "AND av.id != :excludeId")
    boolean existsByClientIdAndAttributeIdAndValueIgnoreCaseExcluding(
        @Param("clientId") Integer clientId,
        @Param("attributeId") Integer attributeId,
        @Param("value") String value,
        @Param("excludeId") Integer excludeId
    );
    
    /**
     * Find enabled attribute value by ID and client ID
     */
    @Query("SELECT av FROM AttributeValue av " +
           "WHERE av.id = :id " +
           "AND av.clientId = :clientId " +
           "AND av.enabled = true")
    Optional<AttributeValue> findEnabledByIdAndClientId(@Param("id") Integer id, @Param("clientId") Integer clientId);
    
    /**
     * Find values by IDs and client ID (for batch operations)
     */
    @Query("SELECT av FROM AttributeValue av " +
           "WHERE av.id IN :ids " +
           "AND av.clientId = :clientId")
    List<AttributeValue> findAllByIdsAndClientId(@Param("ids") List<Integer> ids, @Param("clientId") Integer clientId);
}
