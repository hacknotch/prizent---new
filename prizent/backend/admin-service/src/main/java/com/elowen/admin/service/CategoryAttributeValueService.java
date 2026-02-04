package com.elowen.admin.service;

import com.elowen.admin.dto.AttributeValueResponse;
import com.elowen.admin.entity.AttributeValue;
import com.elowen.admin.entity.Category;
import com.elowen.admin.entity.CategoryAttributeValue;
import com.elowen.admin.exception.CategoryNotFoundException;
import com.elowen.admin.repository.AttributeValueRepository;
import com.elowen.admin.repository.CategoryAttributeRepository;
import com.elowen.admin.repository.CategoryAttributeValueRepository;
import com.elowen.admin.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for CategoryAttributeValue management - assigns attribute values to categories.
 * 
 * CRITICAL SECURITY PRINCIPLES:
 * - ALL operations are scoped by client_id from UserPrincipal
 * - Validates category, attribute, attribute value, and client_id consistency
 * - Ensures all entities belong to same client
 * 
 * Business Rules:
 * - Category must exist and be enabled
 * - Attribute must be assigned to category BEFORE values can be assigned
 * - Attribute value must exist, be enabled, and belong to correct attribute
 * - All entities must belong to same client
 * - No duplicate assignments
 * - Replace operation is atomic (transactional)
 */
@Service
public class CategoryAttributeValueService {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryAttributeValueService.class);
    
    private final CategoryAttributeValueRepository categoryAttributeValueRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    
    @Autowired
    public CategoryAttributeValueService(
            CategoryAttributeValueRepository categoryAttributeValueRepository,
            CategoryRepository categoryRepository,
            AttributeValueRepository attributeValueRepository,
            CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryAttributeValueRepository = categoryAttributeValueRepository;
        this.categoryRepository = categoryRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
    }
    
    /**
     * Assign attribute values to a category (add to existing assignments)
     * 
     * Business Rules:
     * - Category must exist and be enabled
     * - All attribute values must exist and be enabled
     * - Attribute must be assigned to category
     * - All must belong to same client
     * - Skips already assigned values
     */
    @Transactional
    public List<AttributeValueResponse> assignAttributeValuesToCategory(
            Integer categoryId, List<Integer> attributeValueIds, Integer clientId) {
        
        log.info("Assigning {} attribute values to category {} for client {}", 
                attributeValueIds.size(), categoryId, clientId);
        
        // Validate category
        Category category = validateCategory(categoryId, clientId);
        
        // Remove duplicates from input
        Set<Integer> uniqueValueIds = new HashSet<>(attributeValueIds);
        
        // Get attribute values and group by attribute
        List<AttributeValue> attributeValues = attributeValueRepository
                .findAllByIdsAndClientId(new ArrayList<>(uniqueValueIds), clientId);
        
        if (attributeValues.size() != uniqueValueIds.size()) {
            throw new IllegalArgumentException("One or more attribute values not found or not accessible");
        }
        
        // Validate each value and check attribute assignment
        Map<Integer, List<AttributeValue>> valuesByAttribute = new HashMap<>();
        for (AttributeValue value : attributeValues) {
            if (!value.getEnabled()) {
                throw new IllegalArgumentException(
                    String.format("Attribute value %d is disabled and cannot be assigned", value.getId())
                );
            }
            
            valuesByAttribute
                .computeIfAbsent(value.getAttributeId(), k -> new ArrayList<>())
                .add(value);
        }
        
        // Verify all attributes are assigned to the category
        for (Integer attributeId : valuesByAttribute.keySet()) {
            boolean attributeAssigned = categoryAttributeRepository
                    .existsByCategoryIdAndAttributeIdAndClientId(categoryId, attributeId, clientId);
            
            if (!attributeAssigned) {
                throw new IllegalArgumentException(
                    String.format("Attribute %d is not assigned to category %d. " +
                        "Assign the attribute to the category before assigning values.", 
                        attributeId, categoryId)
                );
            }
        }
        
        List<CategoryAttributeValue> newAssignments = new ArrayList<>();
        
        for (AttributeValue value : attributeValues) {
            // Check if already assigned
            if (categoryAttributeValueRepository.existsByCategoryIdAndAttributeValueIdAndClientId(
                    categoryId, value.getId(), clientId)) {
                log.debug("Attribute value {} already assigned to category {} - skipping", 
                        value.getId(), categoryId);
                continue;
            }
            
            // Create mapping
            CategoryAttributeValue categoryAttributeValue = new CategoryAttributeValue(
                clientId, categoryId, value.getAttributeId(), value.getId()
            );
            newAssignments.add(categoryAttributeValue);
        }
        
        // Save all new assignments
        if (!newAssignments.isEmpty()) {
            categoryAttributeValueRepository.saveAll(newAssignments);
            log.info("Successfully assigned {} new attribute values to category {}", 
                    newAssignments.size(), categoryId);
        } else {
            log.info("No new attribute values to assign - all were already assigned");
        }
        
        // Return all current attribute values for this category
        return getAttributeValuesForCategory(categoryId, clientId);
    }
    
    /**
     * Replace all attribute values for a category (atomic operation)
     * 
     * Business Rules:
     * - Removes all existing attribute value assignments
     * - Assigns new attribute values
     * - Atomic transaction - all or nothing
     */
    @Transactional
    public List<AttributeValueResponse> replaceAttributeValuesForCategory(
            Integer categoryId, List<Integer> attributeValueIds, Integer clientId) {
        
        log.info("Replacing attribute values for category {} with {} new values for client {}", 
                categoryId, attributeValueIds.size(), clientId);
        
        // Validate category
        Category category = validateCategory(categoryId, clientId);
        
        // Delete all existing assignments for this category
        categoryAttributeValueRepository.deleteByCategoryIdAndClientId(categoryId, clientId);
        log.debug("Deleted existing attribute value assignments for category {}", categoryId);
        
        // Remove duplicates from input
        Set<Integer> uniqueValueIds = new HashSet<>(attributeValueIds);
        
        // Get attribute values and group by attribute
        List<AttributeValue> attributeValues = attributeValueRepository
                .findAllByIdsAndClientId(new ArrayList<>(uniqueValueIds), clientId);
        
        if (attributeValues.size() != uniqueValueIds.size()) {
            throw new IllegalArgumentException("One or more attribute values not found or not accessible");
        }
        
        // Validate each value and check attribute assignment
        Map<Integer, List<AttributeValue>> valuesByAttribute = new HashMap<>();
        for (AttributeValue value : attributeValues) {
            if (!value.getEnabled()) {
                throw new IllegalArgumentException(
                    String.format("Attribute value %d is disabled and cannot be assigned", value.getId())
                );
            }
            
            valuesByAttribute
                .computeIfAbsent(value.getAttributeId(), k -> new ArrayList<>())
                .add(value);
        }
        
        // Verify all attributes are assigned to the category
        for (Integer attributeId : valuesByAttribute.keySet()) {
            boolean attributeAssigned = categoryAttributeRepository
                    .existsByCategoryIdAndAttributeIdAndClientId(categoryId, attributeId, clientId);
            
            if (!attributeAssigned) {
                throw new IllegalArgumentException(
                    String.format("Attribute %d is not assigned to category %d. " +
                        "Assign the attribute to the category before assigning values.", 
                        attributeId, categoryId)
                );
            }
        }
        
        List<CategoryAttributeValue> newAssignments = new ArrayList<>();
        
        for (AttributeValue value : attributeValues) {
            // Create mapping
            CategoryAttributeValue categoryAttributeValue = new CategoryAttributeValue(
                clientId, categoryId, value.getAttributeId(), value.getId()
            );
            newAssignments.add(categoryAttributeValue);
        }
        
        // Save all new assignments
        if (!newAssignments.isEmpty()) {
            categoryAttributeValueRepository.saveAll(newAssignments);
            log.info("Successfully assigned {} attribute values to category {}", 
                    newAssignments.size(), categoryId);
        } else {
            log.info("No attribute values assigned - category {} now has no attribute values", categoryId);
        }
        
        // Return all current attribute values for this category
        return getAttributeValuesForCategory(categoryId, clientId);
    }
    
    /**
     * Get all attribute values assigned to a category
     */
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAttributeValuesForCategory(Integer categoryId, Integer clientId) {
        log.debug("Fetching attribute values for category {} (client {})", categoryId, clientId);
        
        // Validate category exists
        categoryRepository.findByIdAndClientId(categoryId, clientId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, clientId));
        
        // Get all category-attribute-value mappings
        List<CategoryAttributeValue> categoryAttributeValues = 
                categoryAttributeValueRepository.findAllByCategoryIdAndClientId(categoryId, clientId);
        
        if (categoryAttributeValues.isEmpty()) {
            log.debug("No attribute values assigned to category {}", categoryId);
            return new ArrayList<>();
        }
        
        // Fetch attribute value details
        List<Integer> valueIds = categoryAttributeValues.stream()
                .map(CategoryAttributeValue::getAttributeValueId)
                .collect(Collectors.toList());
        
        List<AttributeValue> values = attributeValueRepository
                .findAllByIdsAndClientId(valueIds, clientId);
        
        log.debug("Found {} attribute values for category {}", values.size(), categoryId);
        
        return values.stream()
                .map(AttributeValueResponse::fromEntity)
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
                String.format("Category %d is disabled and cannot have attribute values assigned", categoryId)
            );
        }
        
        return category;
    }
}
