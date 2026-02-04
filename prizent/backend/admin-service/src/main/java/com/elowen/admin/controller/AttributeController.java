package com.elowen.admin.controller;

import com.elowen.admin.dto.AssignAttributesRequest;
import com.elowen.admin.dto.AttributeResponse;
import com.elowen.admin.dto.CreateAttributeRequest;
import com.elowen.admin.security.UserPrincipal;
import com.elowen.admin.service.AttributeService;
import com.elowen.admin.service.CategoryAttributeService;
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
 * REST Controller for Attribute Management operations
 * 
 * SECURITY:
 * - All endpoints require ADMIN or SUPER_ADMIN role
 * - All operations are tenant-safe using authenticated UserPrincipal
 * - client_id is extracted from JWT, never from request
 * 
 * ENDPOINTS:
 * - POST   /api/admin/categories/attributes - Create attribute
 * - GET    /api/admin/categories/attributes - List all attributes
 * - PATCH  /api/admin/categories/attributes/{id}/enable - Enable attribute
 * - PATCH  /api/admin/categories/attributes/{id}/disable - Disable attribute
 * - POST   /api/admin/categories/{categoryId}/attributes - Assign attributes
 * - GET    /api/admin/categories/{categoryId}/attributes - Get category attributes
 * - PUT    /api/admin/categories/{categoryId}/attributes - Replace category attributes
 */
@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@CrossOrigin(origins = "*")
public class AttributeController {
    
    private static final Logger log = LoggerFactory.getLogger(AttributeController.class);
    
    private final AttributeService attributeService;
    private final CategoryAttributeService categoryAttributeService;
    
    @Autowired
    public AttributeController(AttributeService attributeService, 
                              CategoryAttributeService categoryAttributeService) {
        this.attributeService = attributeService;
        this.categoryAttributeService = categoryAttributeService;
    }
    
    /**
     * Create a new attribute - ADMIN/SUPER_ADMIN only
     * POST /api/admin/categories/attributes
     */
    @PostMapping("/attributes")
    public ResponseEntity<Map<String, Object>> createAttribute(
            @Valid @RequestBody CreateAttributeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("CREATE ATTRIBUTE request from admin: {} for client: {} - name: '{}'", 
                principal.getUsername(), principal.getClientId(), request.getName());
        
        AttributeResponse attributeResponse = attributeService.createAttribute(request, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute created successfully");
        response.put("attribute", attributeResponse);
        
        log.info("Attribute created successfully: ID {} by admin: {}", 
                attributeResponse.getId(), principal.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all attributes for admin's client - ADMIN/SUPER_ADMIN only
     * GET /api/admin/categories/attributes
     */
    @GetMapping("/attributes")
    public ResponseEntity<Map<String, Object>> getAllAttributes(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET ALL ATTRIBUTES request from admin: {} for client: {}", 
                principal.getUsername(), principal.getClientId());
        
        List<AttributeResponse> attributes = attributeService.getAllAttributes(principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attributes retrieved successfully");
        response.put("attributes", attributes);
        response.put("count", attributes.size());
        
        log.debug("Retrieved {} attributes for admin: {}", attributes.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable attribute - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/categories/attributes/{attributeId}/enable
     */
    @PatchMapping("/attributes/{attributeId}/enable")
    public ResponseEntity<Map<String, Object>> enableAttribute(
            @PathVariable Integer attributeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("ENABLE ATTRIBUTE request for attributeId: {} from admin: {} for client: {}", 
                attributeId, principal.getUsername(), principal.getClientId());
        
        AttributeResponse attributeResponse = attributeService.enableAttribute(attributeId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute enabled successfully");
        response.put("attribute", attributeResponse);
        
        log.info("Attribute enabled: ID {} by admin: {}", attributeId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Disable attribute - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/categories/attributes/{attributeId}/disable
     */
    @PatchMapping("/attributes/{attributeId}/disable")
    public ResponseEntity<Map<String, Object>> disableAttribute(
            @PathVariable Integer attributeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("DISABLE ATTRIBUTE request for attributeId: {} from admin: {} for client: {}", 
                attributeId, principal.getUsername(), principal.getClientId());
        
        AttributeResponse attributeResponse = attributeService.disableAttribute(attributeId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attribute disabled successfully");
        response.put("attribute", attributeResponse);
        
        log.info("Attribute disabled: ID {} by admin: {}", attributeId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assign attributes to category - ADMIN/SUPER_ADMIN only
     * POST /api/admin/categories/{categoryId}/attributes
     */
    @PostMapping("/{categoryId}/attributes")
    public ResponseEntity<Map<String, Object>> assignAttributesToCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody AssignAttributesRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("ASSIGN ATTRIBUTES request for categoryId: {} from admin: {} for client: {} - {} attributes", 
                categoryId, principal.getUsername(), principal.getClientId(), request.getAttributeIds().size());
        
        List<AttributeResponse> attributes = categoryAttributeService.assignAttributesToCategory(
                categoryId, request.getAttributeIds(), principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Attributes assigned successfully");
        response.put("attributes", attributes);
        response.put("count", attributes.size());
        
        log.info("Attributes assigned to category {}: {} attributes by admin: {}", 
                categoryId, attributes.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get attributes for category - ADMIN/SUPER_ADMIN only
     * GET /api/admin/categories/{categoryId}/attributes
     */
    @GetMapping("/{categoryId}/attributes")
    public ResponseEntity<Map<String, Object>> getAttributesForCategory(
            @PathVariable Integer categoryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET ATTRIBUTES FOR CATEGORY request for categoryId: {} from admin: {} for client: {}", 
                categoryId, principal.getUsername(), principal.getClientId());
        
        List<AttributeResponse> attributes = categoryAttributeService.getAttributesForCategory(
                categoryId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Category attributes retrieved successfully");
        response.put("attributes", attributes);
        response.put("count", attributes.size());
        
        log.debug("Retrieved {} attributes for category {} by admin: {}", 
                attributes.size(), categoryId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Replace attributes for category - ADMIN/SUPER_ADMIN only
     * PUT /api/admin/categories/{categoryId}/attributes
     */
    @PutMapping("/{categoryId}/attributes")
    public ResponseEntity<Map<String, Object>> replaceAttributesForCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody AssignAttributesRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("REPLACE ATTRIBUTES request for categoryId: {} from admin: {} for client: {} - {} attributes", 
                categoryId, principal.getUsername(), principal.getClientId(), request.getAttributeIds().size());
        
        List<AttributeResponse> attributes = categoryAttributeService.replaceAttributesForCategory(
                categoryId, request.getAttributeIds(), principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Category attributes replaced successfully");
        response.put("attributes", attributes);
        response.put("count", attributes.size());
        
        log.info("Attributes replaced for category {}: {} attributes by admin: {}", 
                categoryId, attributes.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
