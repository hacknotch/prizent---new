package com.elowen.admin.service;

import com.elowen.admin.dto.AttributeResponse;
import com.elowen.admin.entity.Attribute;
import com.elowen.admin.entity.Category;
import com.elowen.admin.entity.CategoryAttribute;
import com.elowen.admin.exception.CategoryNotFoundException;
import com.elowen.admin.repository.AttributeRepository;
import com.elowen.admin.repository.CategoryAttributeRepository;
import com.elowen.admin.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for CategoryAttribute management - assigns attributes to categories.
 * 
 * CRITICAL SECURITY PRINCIPLES:
 * - ALL operations are scoped by client_id from UserPrincipal
 * - Validates category, attribute, and client_id consistency
 * - Ensures all entities belong to same client
 * 
 * Business Rules:
 * - Category must exist and be enabled
 * - Attribute must exist and be enabled
 * - Both must belong to same client
 * - No duplicate assignments
 * - Replace operation is atomic (transactional)
 */
@Service
public class CategoryAttributeService {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryAttributeService.class);
    
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    
    @Autowired
    public CategoryAttributeService(
            CategoryAttributeRepository categoryAttributeRepository,
            CategoryRepository categoryRepository,
            AttributeRepository attributeRepository) {
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.categoryRepository = categoryRepository;
        this.attributeRepository = attributeRepository;
    }
    
    /**
     * Assign attributes to a category (add to existing assignments)
     * 
     * Business Rules:
     * - Category must exist and be enabled
     * - All attributes must exist and be enabled
     * - All must belong to same client
     * - Skips already assigned attributes
     */
    @Transactional
    public List<AttributeResponse> assignAttributesToCategory(
            Integer categoryId, List<Integer> attributeIds, Integer clientId) {
        
        log.info("Assigning {} attributes to category {} for client {}", 
                attributeIds.size(), categoryId, clientId);
        
        // Validate category
        Category category = validateCategory(categoryId, clientId);
        
        // Remove duplicates from input
        Set<Integer> uniqueAttributeIds = new HashSet<>(attributeIds);
        
        List<CategoryAttribute> newAssignments = new ArrayList<>();
        
        for (Integer attributeId : uniqueAttributeIds) {
            // Validate attribute
            Attribute attribute = validateAttribute(attributeId, clientId);
            
            // Check if already assigned
            if (categoryAttributeRepository.existsByCategoryIdAndAttributeIdAndClientId(
                    categoryId, attributeId, clientId)) {
                log.debug("Attribute {} already assigned to category {} - skipping", 
                        attributeId, categoryId);
                continue;
            }
            
            // Create mapping
            CategoryAttribute categoryAttribute = new CategoryAttribute(clientId, categoryId, attributeId);
            newAssignments.add(categoryAttribute);
        }
        
        // Save all new assignments
        if (!newAssignments.isEmpty()) {
            categoryAttributeRepository.saveAll(newAssignments);
            log.info("Successfully assigned {} new attributes to category {}", 
                    newAssignments.size(), categoryId);
        } else {
            log.info("No new attributes to assign - all were already assigned");
        }
        
        // Return all current attributes for this category
        return getAttributesForCategory(categoryId, clientId);
    }
    
    /**
     * Replace all attributes for a category (atomic operation)
     * 
     * Business Rules:
     * - Removes all existing attribute assignments
     * - Assigns new attributes
     * - Atomic transaction - all or nothing
     */
    @Transactional
    public List<AttributeResponse> replaceAttributesForCategory(
            Integer categoryId, List<Integer> attributeIds, Integer clientId) {
        
        log.info("Replacing attributes for category {} with {} new attributes for client {}", 
                categoryId, attributeIds.size(), clientId);
        
        // Validate category
        Category category = validateCategory(categoryId, clientId);
        
        // Delete all existing assignments for this category
        categoryAttributeRepository.deleteByCategoryIdAndClientId(categoryId, clientId);
        log.debug("Deleted existing attribute assignments for category {}", categoryId);
        
        // Remove duplicates from input
        Set<Integer> uniqueAttributeIds = new HashSet<>(attributeIds);
        
        List<CategoryAttribute> newAssignments = new ArrayList<>();
        
        for (Integer attributeId : uniqueAttributeIds) {
            // Validate attribute
            Attribute attribute = validateAttribute(attributeId, clientId);
            
            // Create mapping
            CategoryAttribute categoryAttribute = new CategoryAttribute(clientId, categoryId, attributeId);
            newAssignments.add(categoryAttribute);
        }
        
        // Save all new assignments
        if (!newAssignments.isEmpty()) {
            categoryAttributeRepository.saveAll(newAssignments);
            log.info("Successfully assigned {} attributes to category {}", 
                    newAssignments.size(), categoryId);
        } else {
            log.info("No attributes assigned - category {} now has no attributes", categoryId);
        }
        
        // Return all current attributes for this category
        return getAttributesForCategory(categoryId, clientId);
    }
    
    /**
     * Get all attributes assigned to a category
     */
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAttributesForCategory(Integer categoryId, Integer clientId) {
        log.debug("Fetching attributes for category {} (client {})", categoryId, clientId);
        
        // Validate category exists
        categoryRepository.findByIdAndClientId(categoryId, clientId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, clientId));
        
        // Get all category-attribute mappings
        List<CategoryAttribute> categoryAttributes = 
                categoryAttributeRepository.findAllByCategoryIdAndClientId(categoryId, clientId);
        
        if (categoryAttributes.isEmpty()) {
            log.debug("No attributes assigned to category {}", categoryId);
            return new ArrayList<>();
        }
        
        // Fetch attribute details
        List<Integer> attributeIds = categoryAttributes.stream()
                .map(CategoryAttribute::getAttributeId)
                .collect(Collectors.toList());
        
        List<Attribute> attributes = new ArrayList<>();
        for (Integer attributeId : attributeIds) {
            attributeRepository.findByIdAndClientId(attributeId, clientId)
                    .ifPresent(attributes::add);
        }
        
        log.debug("Found {} attributes for category {}", attributes.size(), categoryId);
        
        return attributes.stream()
                .map(AttributeResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Validate category exists, is enabled, and belongs to client
     */
    private Category validateCategory(Integer categoryId, Integer clientId) {
        Category category = categoryRepository.findByIdAndClientId(categoryId, clientId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, clientId));
        
        if (!category.getEnabled()) {
            throw new IllegalArgumentException(
                String.format("Category %d is disabled and cannot have attributes assigned", categoryId)
            );
        }
        
        return category;
    }
    
    /**
     * Validate attribute exists, is enabled, and belongs to client
     */
    private Attribute validateAttribute(Integer attributeId, Integer clientId) {
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Attribute %d not found for client %d", attributeId, clientId)
                ));
        
        if (!attribute.getEnabled()) {
            throw new IllegalArgumentException(
                String.format("Attribute %d is disabled and cannot be assigned", attributeId)
            );
        }
        
        return attribute;
    }
}
