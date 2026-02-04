package com.elowen.admin.controller;

import com.elowen.admin.dto.CreateProductRequest;
import com.elowen.admin.dto.ProductResponse;
import com.elowen.admin.dto.UpdateProductRequest;
import com.elowen.admin.security.UserPrincipal;
import com.elowen.admin.service.ProductService;
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
 * REST Controller for Product Management operations
 * 
 * SECURITY:
 * - All endpoints require ADMIN or SUPER_ADMIN role
 * - All operations are tenant-safe using authenticated UserPrincipal
 * - client_id is extracted from JWT, never from request
 * 
 * ENDPOINTS:
 * - POST   /api/admin/products - Create product
 * - GET    /api/admin/products - List all products
 * - GET    /api/admin/products/{productId} - Get product details
 * - PUT    /api/admin/products/{productId} - Update product
 * - PATCH  /api/admin/products/{productId}/enable - Enable product
 * - PATCH  /api/admin/products/{productId}/disable - Disable product
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@CrossOrigin(origins = "*")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Create a new product - ADMIN/SUPER_ADMIN only
     * POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("CREATE PRODUCT request from admin: {} for client: {} - category: {}, name: '{}'", 
                principal.getUsername(), principal.getClientId(), request.getCategoryId(), request.getName());
        
        ProductResponse productResponse = productService.createProduct(request, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product created successfully");
        response.put("product", productResponse);
        
        log.info("Product created successfully: ID {} by admin: {}", 
                productResponse.getId(), principal.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all products for admin's client - ADMIN/SUPER_ADMIN only
     * GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET ALL PRODUCTS request from admin: {} for client: {}", 
                principal.getUsername(), principal.getClientId());
        
        List<ProductResponse> products = productService.getAllProducts(principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Products retrieved successfully");
        response.put("products", products);
        response.put("count", products.size());
        
        log.debug("Retrieved {} products for admin: {}", products.size(), principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by ID - ADMIN/SUPER_ADMIN only
     * GET /api/admin/products/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(
            @PathVariable Integer productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.debug("GET PRODUCT request for productId: {} from admin: {} for client: {}", 
                productId, principal.getUsername(), principal.getClientId());
        
        ProductResponse product = productService.getProductById(productId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product retrieved successfully");
        response.put("product", product);
        
        log.debug("Retrieved product {} for admin: {}", productId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update product - ADMIN/SUPER_ADMIN only
     * PUT /api/admin/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("UPDATE PRODUCT request for productId: {} from admin: {} for client: {}", 
                productId, principal.getUsername(), principal.getClientId());
        
        ProductResponse productResponse = productService.updateProduct(
                productId, request, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product updated successfully");
        response.put("product", productResponse);
        
        log.info("Product updated successfully: ID {} by admin: {}", 
                productId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable product - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/products/{productId}/enable
     */
    @PatchMapping("/{productId}/enable")
    public ResponseEntity<Map<String, Object>> enableProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("ENABLE PRODUCT request for productId: {} from admin: {} for client: {}", 
                productId, principal.getUsername(), principal.getClientId());
        
        ProductResponse productResponse = productService.enableProduct(productId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product enabled successfully");
        response.put("product", productResponse);
        
        log.info("Product enabled: ID {} by admin: {}", productId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Disable product - ADMIN/SUPER_ADMIN only
     * PATCH /api/admin/products/{productId}/disable
     */
    @PatchMapping("/{productId}/disable")
    public ResponseEntity<Map<String, Object>> disableProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("DISABLE PRODUCT request for productId: {} from admin: {} for client: {}", 
                productId, principal.getUsername(), principal.getClientId());
        
        ProductResponse productResponse = productService.disableProduct(productId, principal.getClientId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product disabled successfully");
        response.put("product", productResponse);
        
        log.info("Product disabled: ID {} by admin: {}", productId, principal.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
