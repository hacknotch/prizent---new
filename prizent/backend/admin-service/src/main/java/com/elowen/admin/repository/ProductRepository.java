package com.elowen.admin.repository;

import com.elowen.admin.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entities with STRICT tenant isolation.
 * 
 * CRITICAL SECURITY RULES:
 * - ALL queries MUST include client_id for tenant safety
 * - NO findById() methods without tenant isolation
 * - ALL operations are scoped to the authenticated client
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    /**
     * Find product by ID within client's tenant boundary
     * SECURITY: Prevents cross-tenant data access
     */
    Optional<Product> findByIdAndClientId(Integer id, Integer clientId);
    
    /**
     * Find all products for a specific client
     */
    List<Product> findAllByClientIdOrderByCreateDateTimeDesc(Integer clientId);
    
    /**
     * Find only enabled products for a client
     */
    List<Product> findAllByClientIdAndEnabledTrueOrderByNameAsc(Integer clientId);
    
    /**
     * Find products by category
     */
    List<Product> findAllByCategoryIdAndClientIdOrderByNameAsc(Integer categoryId, Integer clientId);
    
    /**
     * Check if product name exists within category (case-insensitive)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p " +
           "WHERE p.clientId = :clientId " +
           "AND p.categoryId = :categoryId " +
           "AND LOWER(p.name) = LOWER(:name)")
    boolean existsByClientIdAndCategoryIdAndNameIgnoreCase(
        @Param("clientId") Integer clientId,
        @Param("categoryId") Integer categoryId,
        @Param("name") String name
    );
    
    /**
     * Check if product name exists within category excluding specific product ID
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p " +
           "WHERE p.clientId = :clientId " +
           "AND p.categoryId = :categoryId " +
           "AND LOWER(p.name) = LOWER(:name) " +
           "AND p.id != :excludeId")
    boolean existsByClientIdAndCategoryIdAndNameIgnoreCaseExcluding(
        @Param("clientId") Integer clientId,
        @Param("categoryId") Integer categoryId,
        @Param("name") String name,
        @Param("excludeId") Integer excludeId
    );
    
    /**
     * Find enabled product by ID and client ID
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.id = :id " +
           "AND p.clientId = :clientId " +
           "AND p.enabled = true")
    Optional<Product> findEnabledByIdAndClientId(@Param("id") Integer id, @Param("clientId") Integer clientId);
}
