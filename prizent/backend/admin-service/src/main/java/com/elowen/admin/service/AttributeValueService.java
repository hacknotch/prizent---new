package com.elowen.admin.service;

import com.elowen.admin.dto.AttributeValueResponse;
import com.elowen.admin.dto.CreateAttributeValueRequest;
import com.elowen.admin.entity.Attribute;
import com.elowen.admin.entity.AttributeValue;
import com.elowen.admin.exception.AttributeNotFoundException;
import com.elowen.admin.exception.AttributeValueNotFoundException;
import com.elowen.admin.repository.AttributeRepository;
import com.elowen.admin.repository.AttributeValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for AttributeValue management with strict tenant isolation.
 * 
 * CRITICAL SECURITY PRINCIPLES:
 * - ALL operations are scoped by client_id from UserPrincipal
 * - NO operation can access attribute values from other clients
 * - client_id is NEVER accepted from request parameters
 * 
 * Business Rules Enforced:
 * - Attribute must exist, be enabled, and belong to same client
 * - Value must be unique per attribute (case-insensitive)
 * - Soft delete only - no physical deletion
 * - Attribute values default to enabled=true on creation
 */
@Service
public class AttributeValueService {
    
    private static final Logger log = LoggerFactory.getLogger(AttributeValueService.class);
    
    private final AttributeValueRepository attributeValueRepository;
    private final AttributeRepository attributeRepository;
    
    @Autowired
    public AttributeValueService(
            AttributeValueRepository attributeValueRepository,
            AttributeRepository attributeRepository) {
        this.attributeValueRepository = attributeValueRepository;
        this.attributeRepository = attributeRepository;
    }
    
    /**
     * Create a new attribute value for the authenticated client.
     * 
     * Business Rules:
     * - Attribute must exist, be enabled, and belong to client
     * - Value must be unique per attribute (case-insensitive)
     * - Value is enabled by default
     * - client_id comes from authentication context
     */
    @Transactional
    public AttributeValueResponse createAttributeValue(
            Integer attributeId, CreateAttributeValueRequest request, Integer clientId) {
        
        log.info("Creating attribute value '{}' for attribute {} (client {})", 
                request.getValue(), attributeId, clientId);
        
        // Validate attribute exists, is enabled, and belongs to client
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        if (!attribute.getEnabled()) {
            log.warn("Cannot create value for disabled attribute {}", attributeId);
            throw new IllegalArgumentException(
                String.format("Attribute %d is disabled and cannot have values added", attributeId)
            );
        }
        
        String value = request.getValue().trim();
        
        // Validate value uniqueness (case-insensitive)
        if (attributeValueRepository.existsByClientIdAndAttributeIdAndValueIgnoreCase(
                clientId, attributeId, value)) {
            log.warn("Attribute value creation failed - value '{}' already exists for attribute {}", 
                    value, attributeId);
            throw new IllegalArgumentException(
                String.format("Value '%s' already exists for this attribute", value)
            );
        }
        
        // Create attribute value entity
        AttributeValue attributeValue = new AttributeValue(clientId, attributeId, value);
        
        // Save and return response
        AttributeValue savedValue = attributeValueRepository.save(attributeValue);
        
        log.info("Successfully created attribute value with ID {} for attribute {} (client {})", 
                savedValue.getId(), attributeId, clientId);
        
        return AttributeValueResponse.fromEntity(savedValue);
    }
    
    /**
     * Get all values for a specific attribute (includes disabled)
     */
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAllValuesForAttribute(Integer attributeId, Integer clientId) {
        log.debug("Fetching all values for attribute {} (client {})", attributeId, clientId);
        
        // Validate attribute exists and belongs to client
        attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        List<AttributeValue> values = attributeValueRepository
                .findAllByAttributeIdAndClientIdOrderByValueAsc(attributeId, clientId);
        
        log.debug("Found {} values for attribute {}", values.size(), attributeId);
        
        return values.stream()
                .map(AttributeValueResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only enabled values for a specific attribute
     */
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getEnabledValuesForAttribute(Integer attributeId, Integer clientId) {
        log.debug("Fetching enabled values for attribute {} (client {})", attributeId, clientId);
        
        // Validate attribute exists and belongs to client
        attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        List<AttributeValue> values = attributeValueRepository
                .findAllByAttributeIdAndClientIdAndEnabledTrueOrderByValueAsc(attributeId, clientId);
        
        log.debug("Found {} enabled values for attribute {}", values.size(), attributeId);
        
        return values.stream()
                .map(AttributeValueResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Enable (activate) an attribute value
     */
    @Transactional
    public AttributeValueResponse enableAttributeValue(Integer attributeValueId, Integer clientId) {
        log.info("Enabling attribute value {} for client {}", attributeValueId, clientId);
        
        AttributeValue attributeValue = attributeValueRepository.findByIdAndClientId(attributeValueId, clientId)
                .orElseThrow(() -> new AttributeValueNotFoundException(attributeValueId, clientId));
        
        if (attributeValue.getEnabled()) {
            log.info("Attribute value {} is already enabled", attributeValueId);
            return AttributeValueResponse.fromEntity(attributeValue);
        }
        
        attributeValue.setEnabled(true);
        AttributeValue enabledValue = attributeValueRepository.save(attributeValue);
        
        log.info("Successfully enabled attribute value {} for client {}", attributeValueId, clientId);
        
        return AttributeValueResponse.fromEntity(enabledValue);
    }
    
    /**
     * Disable (soft delete) an attribute value
     */
    @Transactional
    public AttributeValueResponse disableAttributeValue(Integer attributeValueId, Integer clientId) {
        log.info("Disabling attribute value {} for client {}", attributeValueId, clientId);
        
        AttributeValue attributeValue = attributeValueRepository.findByIdAndClientId(attributeValueId, clientId)
                .orElseThrow(() -> new AttributeValueNotFoundException(attributeValueId, clientId));
        
        if (!attributeValue.getEnabled()) {
            log.info("Attribute value {} is already disabled", attributeValueId);
            return AttributeValueResponse.fromEntity(attributeValue);
        }
        
        attributeValue.setEnabled(false);
        AttributeValue disabledValue = attributeValueRepository.save(attributeValue);
        
        log.info("Successfully disabled attribute value {} for client {}", attributeValueId, clientId);
        
        return AttributeValueResponse.fromEntity(disabledValue);
    }
    
    /**
     * Validate that attribute value exists, is enabled, and belongs to client
     * Used by CategoryAttributeValueService before assignment
     */
    public AttributeValue validateAttributeValue(Integer attributeValueId, Integer clientId) {
        AttributeValue attributeValue = attributeValueRepository.findByIdAndClientId(attributeValueId, clientId)
                .orElseThrow(() -> new AttributeValueNotFoundException(attributeValueId, clientId));
        
        if (!attributeValue.getEnabled()) {
            throw new IllegalArgumentException(
                String.format("Attribute value %d is disabled and cannot be assigned", attributeValueId)
            );
        }
        
        return attributeValue;
    }
}
