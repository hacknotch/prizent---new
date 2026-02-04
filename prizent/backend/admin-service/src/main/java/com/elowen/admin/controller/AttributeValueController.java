package com.elowen.admin.controller;

import com.elowen.admin.dto.AssignAttributeValuesRequest;
import com.elowen.admin.dto.AttributeValueResponse;
import com.elowen.admin.dto.CreateAttributeValueRequest;
import com.elowen.admin.security.UserPrincipal;
import com.elowen.admin.service.AttributeValueService;
import com.elowen.admin.service.CategoryAttributeValueService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Attribute Value Management operations
 * 
 * SECURITY:
 * - All endpoints require ADMIN or SUPER_ADMIN role
 * - All operations are tenant-safe using authenticated UserPrincipal
 * - client_id is extracted from JWT, never from request
 * 
 * ENDPOINTS:
 * - POST   /api/admin/attributes/{attributeId}/values - Create attribute value
 * - GET    /api/admin/attributes/{attributeId}/values - List values for attribute
 * - PATCH  /api/admin/attributes/values/{valueId}/enable - Enable value
 * - PATCH  /api/admin/attributes/values/{valueId}/disable - Disable value
 * - POST   /api/admin/attributes/categories/{categoryId}/attribute-values - Assign values to category
 * - GET    /api/admin/attributes/categories/{categoryId}/attribute-values - Get category values
 * - PUT    /api/admin/attributes/categories/{categoryId}/attribute-values - Replace category values
 */
@RestController
@RequestMapping("/api/admin/attributes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@CrossOrigin(origins = "*")
public class AttributeValueController {
    
    private static final Logger log = LoggerFactory.getLogger(AttributeValueController.class);
    
    private final AttributeValueService attributeValueService;
    private final CategoryAttributeValueService categoryAttributeValueService;
    
    @Autowired
    public AttributeValueController(
            AttributeValueService attributeValueService,
            CategoryAttributeValueService categoryAttributeValueService) {
        this.attributeValueService = attributeValueService;
        this.categoryAttributeValueService = categoryAttributeValueService;
    }
    
    /**
     * Create a new attribute value - ADMIN/SUPER_ADMIN only
     * POST /api/admin/attributes/{attributeId}/values
     */
    @PostMapping("/{attributeId}/values")
    public ResponseEntity<Map<String, Object>> createAttributeValue(
            @PathVariable Integer attributeId,
            @Valid @RequestBody CreateAttributeValueRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("CREATE ATTRIBUTE VALUE request from admin: {} for client: {} - attribute: {}, value: '{}'", 
                principal.getUsername(), principal.getClientId(), attributeId, request.getValue());
        
        AttributeValueResponse valueResponse = attributeValueService.createAttributeValue(
                attributeId, request, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute value created successfully");
        response.put("attributeValue", valueResponse);
        
        log.info("Attribute value created successfully: ID {} by admin: {}", 
                valueResponse.getId(), principal.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all values for an attribute - ADMIN/SUPER_ADMIN only
     * GET /api/admin/attributes/{attributeId}/values
     */
    @GetMapping("/{attributeId}/values")
    public ResponseEntity<Map<String, Object>> getAttributeValues(
            @PathVariable Integer attributeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET ATTRIBUTE VALUES request from admin: {} for client: {} - attribute: {}", 
                principal.getUsername(), principal.getClientId(), attributeId);
        
        List<AttributeValueResponse> values = attributeValueService.getAllValuesForAttribute(
                attributeId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute values retrieved successfully");
        response.put("values", values);
        response.put("count", values.size());
        
        log.debug("Retrieved {} values for attribute {} by admin: {}", 
                values.size(), attributeId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable attribute value - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/attributes/values/{valueId}/enable
     */
    @PatchMapping("/values/{valueId}/enable")
    public ResponseEntity<Map<String, Object>> enableAttributeValue(
            @PathVariable Integer valueId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("ENABLE ATTRIBUTE VALUE request for valueId: {} from admin: {} for client: {}", 
                valueId, principal.getUsername(), principal.getClientId());
        
        AttributeValueResponse valueResponse = attributeValueService.enableAttributeValue(
                valueId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute value enabled successfully");
        response.put("attributeValue", valueResponse);
        
        log.info("Attribute value enabled: ID {} by admin: {}", valueId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Disable attribute value - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/attributes/values/{valueId}/disable
     */
    @PatchMapping("/values/{valueId}/disable")
    public ResponseEntity<Map<String, Object>> disableAttributeValue(
            @PathVariable Integer valueId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("DISABLE ATTRIBUTE VALUE request for valueId: {} from admin: {} for client: {}", 
                valueId, principal.getUsername(), principal.getClientId());
        
        AttributeValueResponse valueResponse = attributeValueService.disableAttributeValue(
                valueId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute value disabled successfully");
        response.put("attributeValue", valueResponse);
        
        log.info("Attribute value disabled: ID {} by admin: {}", valueId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assign attribute values to category - ADMIN/SUPER_ADMIN only
     * POST /api/admin/attributes/categories/{categoryId}/attribute-values
     */
    @PostMapping("/categories/{categoryId}/attribute-values")
    public ResponseEntity<Map<String, Object>> assignAttributeValuesToCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody AssignAttributeValuesRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("ASSIGN ATTRIBUTE VALUES request for categoryId: {} from admin: {} for client: {} - {} values", 
                categoryId, principal.getUsername(), principal.getClientId(), request.getAttributeValueIds().size());
        
        List<AttributeValueResponse> values = categoryAttributeValueService.assignAttributeValuesToCategory(
                categoryId, request.getAttributeValueIds(), principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute values assigned successfully");
        response.put("values", values);
        response.put("count", values.size());
        
        log.info("Attribute values assigned to category {}: {} values by admin: {}", 
                categoryId, values.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get attribute values for category - ADMIN/SUPER_ADMIN only
     * GET /api/admin/attributes/categories/{categoryId}/attribute-values
     */
    @GetMapping("/categories/{categoryId}/attribute-values")
    public ResponseEntity<Map<String, Object>> getAttributeValuesForCategory(
            @PathVariable Integer categoryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET ATTRIBUTE VALUES FOR CATEGORY request for categoryId: {} from admin: {} for client: {}", 
                categoryId, principal.getUsername(), principal.getClientId());
        
        List<AttributeValueResponse> values = categoryAttributeValueService.getAttributeValuesForCategory(
                categoryId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Category attribute values retrieved successfully");
        response.put("values", values);
        response.put("count", values.size());
        
        log.debug("Retrieved {} attribute values for category {} by admin: {}", 
                values.size(), categoryId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Replace attribute values for category - ADMIN/SUPER_ADMIN only
     * PUT /api/admin/attributes/categories/{categoryId}/attribute-values
     */
    @PutMapping("/categories/{categoryId}/attribute-values")
    public ResponseEntity<Map<String, Object>> replaceAttributeValuesForCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody AssignAttributeValuesRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("REPLACE ATTRIBUTE VALUES request for categoryId: {} from admin: {} for client: {} - {} values", 
                categoryId, principal.getUsername(), principal.getClientId(), request.getAttributeValueIds().size());
        
        List<AttributeValueResponse> values = categoryAttributeValueService.replaceAttributeValuesForCategory(
                categoryId, request.getAttributeValueIds(), principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Category attribute values replaced successfully");
        response.put("values", values);
        response.put("count", values.size());
        
        log.info("Attribute values replaced for category {}: {} values by admin: {}", 
                categoryId, values.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
