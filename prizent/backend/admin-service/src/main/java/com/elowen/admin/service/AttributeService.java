package com.elowen.admin.service;

import com.elowen.admin.dto.AttributeResponse;
import com.elowen.admin.dto.CreateAttributeRequest;
import com.elowen.admin.entity.Attribute;
import com.elowen.admin.exception.AttributeNotFoundException;
import com.elowen.admin.repository.AttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Attribute management with strict tenant isolation.
 * 
 * CRITICAL SECURITY PRINCIPLES:
 * - ALL operations are scoped by client_id from UserPrincipal
 * - NO operation can access attributes from other clients
 * - client_id is NEVER accepted from request parameters
 * 
 * Business Rules Enforced:
 * - Attribute names must be unique per client (case-insensitive)
 * - Soft delete only - no physical deletion
 * - Attributes default to enabled=true on creation
 */
@Service
public class AttributeService {
    
    private static final Logger log = LoggerFactory.getLogger(AttributeService.class);
    
    private final AttributeRepository attributeRepository;
    
    @Autowired
    public AttributeService(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }
    
    /**
     * Create a new attribute for the authenticated client.
     * 
     * Business Rules:
     * - Name must be unique per client (case-insensitive)
     * - Attribute is enabled by default
     * - client_id comes from authentication context
     */
    @Transactional
    public AttributeResponse createAttribute(CreateAttributeRequest request, Integer clientId) {
        log.info("Creating attribute '{}' for client {}", request.getName(), clientId);
        
        String attributeName = request.getName().trim();
        
        // Validate name uniqueness
        if (attributeRepository.existsByClientIdAndNameIgnoreCase(clientId, attributeName)) {
            log.warn("Attribute creation failed - name '{}' already exists for client {}", 
                    attributeName, clientId);
            throw new IllegalArgumentException(
                String.format("Attribute '%s' already exists for this client", attributeName)
            );
        }
        
        // Create attribute entity
        Attribute attribute = new Attribute(clientId, attributeName);
        
        // Save and return response
        Attribute savedAttribute = attributeRepository.save(attribute);
        
        log.info("Successfully created attribute with ID {} for client {}", 
                savedAttribute.getId(), clientId);
        
        return AttributeResponse.fromEntity(savedAttribute);
    }
    
    /**
     * Get all attributes for the authenticated client.
     */
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAllAttributes(Integer clientId) {
        log.debug("Fetching all attributes for client {}", clientId);
        
        List<Attribute> attributes = attributeRepository.findAllByClientIdOrderByCreateDateTimeDesc(clientId);
        
        log.debug("Found {} attributes for client {}", attributes.size(), clientId);
        
        return attributes.stream()
                .map(AttributeResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only enabled attributes for the authenticated client.
     */
    @Transactional(readOnly = true)
    public List<AttributeResponse> getEnabledAttributes(Integer clientId) {
        log.debug("Fetching enabled attributes for client {}", clientId);
        
        List<Attribute> attributes = attributeRepository.findAllByClientIdAndEnabledTrueOrderByNameAsc(clientId);
        
        log.debug("Found {} enabled attributes for client {}", attributes.size(), clientId);
        
        return attributes.stream()
                .map(AttributeResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific attribute by ID within client's tenant boundary.
     */
    @Transactional(readOnly = true)
    public AttributeResponse getAttributeById(Integer attributeId, Integer clientId) {
        log.debug("Fetching attribute {} for client {}", attributeId, clientId);
        
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> {
                    log.warn("Attribute {} not found for client {}", attributeId, clientId);
                    return new AttributeNotFoundException(attributeId, clientId);
                });
        
        return AttributeResponse.fromEntity(attribute);
    }
    
    /**
     * Enable (activate) an attribute
     */
    @Transactional
    public AttributeResponse enableAttribute(Integer attributeId, Integer clientId) {
        log.info("Enabling attribute {} for client {}", attributeId, clientId);
        
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        if (attribute.getEnabled()) {
            log.info("Attribute {} is already enabled", attributeId);
            return AttributeResponse.fromEntity(attribute);
        }
        
        attribute.setEnabled(true);
        Attribute enabledAttribute = attributeRepository.save(attribute);
        
        log.info("Successfully enabled attribute {} for client {}", attributeId, clientId);
        
        return AttributeResponse.fromEntity(enabledAttribute);
    }
    
    /**
     * Disable (soft delete) an attribute
     */
    @Transactional
    public AttributeResponse disableAttribute(Integer attributeId, Integer clientId) {
        log.info("Disabling attribute {} for client {}", attributeId, clientId);
        
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        if (!attribute.getEnabled()) {
            log.info("Attribute {} is already disabled", attributeId);
            return AttributeResponse.fromEntity(attribute);
        }
        
        attribute.setEnabled(false);
        Attribute disabledAttribute = attributeRepository.save(attribute);
        
        log.info("Successfully disabled attribute {} for client {}", attributeId, clientId);
        
        return AttributeResponse.fromEntity(disabledAttribute);
    }
    
    /**
     * Validate that attribute exists, is enabled, and belongs to client
     * Used by CategoryAttributeService before assignment
     */
    public Attribute validateAttribute(Integer attributeId, Integer clientId) {
        Attribute attribute = attributeRepository.findByIdAndClientId(attributeId, clientId)
                .orElseThrow(() -> new AttributeNotFoundException(attributeId, clientId));
        
        if (!attribute.getEnabled()) {
            throw new IllegalArgumentException(
                String.format("Attribute %d is disabled and cannot be assigned", attributeId)
            );
        }
        
        return attribute;
    }
}
