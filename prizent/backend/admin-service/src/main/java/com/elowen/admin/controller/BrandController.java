package com.elowen.admin.controller;

import com.elowen.admin.context.ClientContext;
import com.elowen.admin.dto.BrandResponse;
import com.elowen.admin.dto.CreateBrandRequest;
import com.elowen.admin.dto.UpdateBrandRequest;
import com.elowen.admin.entity.Brand;
import com.elowen.admin.repository.BrandRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    /**
     * Get all brands for the current client
     * GET /api/admin/brands
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBrands() {
        Integer clientId = ClientContext.getClientId();
        if (clientId == null) {
            clientId = 1; // Default clientId for testing
        }
        System.out.println("GET ALL BRANDS request for clientId: " + clientId);
        
        List<Brand> brands = brandRepository.findByClientIdOrderByCreateDateTimeDesc(clientId);
        
        List<BrandResponse> brandResponses = brands.stream()
                .map(BrandResponse::new)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Brands retrieved successfully");
        response.put("brands", brandResponses);
        response.put("count", brandResponses.size());
        
        System.out.println("Retrieved " + brandResponses.size() + " brands for clientId: " + clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get brand by ID
     * GET /api/admin/brands/{brandId}
     */
    @GetMapping("/{brandId}")
    public ResponseEntity<Map<String, Object>> getBrandById(@PathVariable Long brandId) {
        Integer clientId = ClientContext.getClientId();
        if (clientId == null) {
            clientId = 1; // Default clientId for testing
        }
        System.out.println("GET BRAND BY ID request for brandId: " + brandId + ", clientId: " + clientId);
        
        Optional<Brand> brandOpt = brandRepository.findByIdAndClientId(brandId, clientId);
        if (brandOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Brand not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        BrandResponse brandResponse = new BrandResponse(brandOpt.get());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Brand retrieved successfully");
        response.put("brand", brandResponse);
        
        System.out.println("Brand retrieved: " + brandResponse.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new brand
     * POST /api/admin/brands
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        Integer clientId = ClientContext.getClientId();
        if (clientId == null) {
            clientId = 1; // Default clientId for testing
        }
        System.out.println("CREATE BRAND request for clientId: " + clientId);
        System.out.println("Brand name: " + request.getName());
        
        // Check if brand name already exists
        Optional<Brand> existingBrand = brandRepository.findByNameAndClientId(request.getName(), clientId);
        if (existingBrand.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Brand name already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        // Create new brand
        Brand brand = new Brand();
        brand.setClientId(clientId);
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        
        Brand savedBrand = brandRepository.save(brand);
        BrandResponse brandResponse = new BrandResponse(savedBrand);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Brand created successfully");
        response.put("brand", brandResponse);
        
        System.out.println("Brand created: ID " + savedBrand.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a brand
     * PUT /api/admin/brands/{brandId}
     */
    @PutMapping("/{brandId}")
    public ResponseEntity<Map<String, Object>> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody UpdateBrandRequest request) {
        
        Integer clientId = ClientContext.getClientId();
        if (clientId == null) {
            clientId = 1; // Default clientId for testing
        }
        System.out.println("UPDATE BRAND request for brandId: " + brandId + ", clientId: " + clientId);
        
        Optional<Brand> brandOpt = brandRepository.findByIdAndClientId(brandId, clientId);
        if (brandOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Brand not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Brand brand = brandOpt.get();
        
        // Update fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // Check if new name already exists for another brand
            Optional<Brand> existingBrand = brandRepository.findByNameAndClientId(request.getName(), clientId);
            if (existingBrand.isPresent() && !existingBrand.get().getId().equals(brandId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Brand name already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            brand.setName(request.getName());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }
        if (request.getEnabled() != null) {
            brand.setEnabled(request.getEnabled());
        }
        
        Brand updatedBrand = brandRepository.save(brand);
        BrandResponse brandResponse = new BrandResponse(updatedBrand);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Brand updated successfully");
        response.put("brand", brandResponse);
        
        System.out.println("Brand updated: ID " + updatedBrand.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a brand
     * DELETE /api/admin/brands/{brandId}
     */
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable Long brandId) {
        Integer clientId = ClientContext.getClientId();
        if (clientId == null) {
            clientId = 1; // Default clientId for testing
        }
        System.out.println("DELETE BRAND request for brandId: " + brandId + ", clientId: " + clientId);
        
        Optional<Brand> brandOpt = brandRepository.findByIdAndClientId(brandId, clientId);
        if (brandOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Brand not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        brandRepository.delete(brandOpt.get());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Brand deleted successfully");
        
        System.out.println("Brand deleted: ID " + brandId);
        return ResponseEntity.ok(response);
    }
}
