package com.elowen.admin.repository;

import com.elowen.admin.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Attribute entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - NO findById() methods without tenant isolation
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Integer> {
    
    /**
     * Find attribute by ID within client's tenant boundary
     * SECURITY: Prevents cross-tenant data access
     */
    Optional<Attribute> findByIdAndClientId(Integer id, Integer clientId);
    
    /**
     * Find all attributes for a specific client (including disabled)
     */
    List<Attribute> findAllByClientIdOrderByCreateDateTimeDesc(Integer clientId);
    
    /**
     * Find only active (enabled) attributes for a client
     */
    List<Attribute> findAllByClientIdAndEnabledTrueOrderByNameAsc(Integer clientId);
    
    /**
     * Check if attribute name exists for client (case-insensitive)
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attribute a " +
           "WHERE a.clientId = :clientId AND LOWER(a.name) = LOWER(:name)")
    boolean existsByClientIdAndNameIgnoreCase(@Param("clientId") Integer clientId, @Param("name") String name);
    
    /**
     * Check if attribute name exists for client excluding specific attribute ID
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attribute a " +
           "WHERE a.clientId = :clientId AND LOWER(a.name) = LOWER(:name) AND a.id != :excludeId")
    boolean existsByClientIdAndNameIgnoreCaseExcluding(
        @Param("clientId") Integer clientId,
        @Param("name") String name,
        @Param("excludeId") Integer excludeId
    );
    
    /**
     * Find enabled attribute by ID and client ID
     */
    @Query("SELECT a FROM Attribute a WHERE a.id = :id AND a.clientId = :clientId AND a.enabled = true")
    Optional<Attribute> findEnabledByIdAndClientId(@Param("id") Integer id, @Param("clientId") Integer clientId);
}
