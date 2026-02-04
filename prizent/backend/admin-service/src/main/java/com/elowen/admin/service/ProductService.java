package com.elowen.admin.service;

import com.elowen.admin.dto.*;
import com.elowen.admin.entity.*;
import com.elowen.admin.exception.CategoryNotFoundException;
import com.elowen.admin.exception.ProductNotFoundException;
import com.elowen.admin.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Product management with strict tenant isolation.
 * 
 * CRITICAL SECURITY PRINCIPLES:
 * - ALL operations are scoped by client_id from UserPrincipal
 * - NO operation can access products from other clients
 * - client_id is NEVER accepted from request parameters
 * 
 * CRITICAL BUSINESS RULES (NON-NEGOTIABLE):
 * - Products belong to ONE category
 * - Products can ONLY use attributes assigned to that category
 * - Products can ONLY use attribute values assigned to that category
 * - Backend MUST enforce these rules - frontend validation is NOT enough
 * 
 * Business Rules Enforced:
 * - Product name unique per (client_id, category_id) - case-insensitive
 * - Category must exist, be enabled, and belong to same client
 * - All attributes must be assigned to the category
 * - All attribute values must: belong to attribute, be enabled, be assigned to category
 * - Soft delete only - no physical deletion
 */
@Service
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryAttributeValueRepository categoryAttributeValueRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    
    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ProductAttributeRepository productAttributeRepository,
            CategoryRepository categoryRepository,
            CategoryAttributeRepository categoryAttributeRepository,
            CategoryAttributeValueRepository categoryAttributeValueRepository,
            AttributeRepository attributeRepository,
            AttributeValueRepository attributeValueRepository) {
        this.productRepository = productRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.categoryAttributeValueRepository = categoryAttributeValueRepository;
        this.attributeRepository = attributeRepository;
        this.attributeValueRepository = attributeValueRepository;
    }
    
    /**
     * Create a new product with strict validation.
     * 
     * Validation Rules:
     * 1. Category must exist, be enabled, belong to client
     * 2. Product name must be unique within category (case-insensitive)
     * 3. Each attribute must be assigned to category
     * 4. Each attribute value must: belong to attribute, be enabled, be assigned to category
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, Integer clientId) {
        log.info("Creating product '{}' for category {} (client {})", 
                request.getName(), request.getCategoryId(), clientId);
        
        // 1. Validate category
        Category category = validateCategory(request.getCategoryId(), clientId);
        
        String productName = request.getName().trim();
        
        // 2. Validate name uniqueness within category
        if (productRepository.existsByClientIdAndCategoryIdAndNameIgnoreCase(
                clientId, request.getCategoryId(), productName)) {
            log.warn("Product creation failed - name '{}' already exists in category {}", 
                    productName, request.getCategoryId());
            throw new IllegalArgumentException(
                String.format("Product '%s' already exists in this category", productName)
            );
        }
        
        // 3. Create product entity
        Product product = new Product(
            clientId, 
            request.getCategoryId(), 
            productName, 
            request.getDescription()
        );
        
        Product savedProduct = productRepository.save(product);
        
        log.info("Successfully created product with ID {} for client {}", 
                savedProduct.getId(), clientId);
        
        // 4. Validate and assign attributes
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            assignProductAttributes(savedProduct.getId(), request.getCategoryId(), 
                    request.getAttributes(), clientId);
        }
        
        // 5. Return product response with attributes
        return buildProductResponse(savedProduct, category.getName(), clientId);
    }
    
    /**
     * Get all products for the authenticated client.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Integer clientId) {
        log.debug("Fetching all products for client {}", clientId);
        
        List<Product> products = productRepository.findAllByClientIdOrderByCreateDateTimeDesc(clientId);
        
        log.debug("Found {} products for client {}", products.size(), clientId);
        
        return products.stream()
                .map(product -> {
                    Category category = categoryRepository.findByIdAndClientId(
                            product.getCategoryId(), clientId).orElse(null);
                    String categoryName = category != null ? category.getName() : "Unknown";
                    return buildProductResponse(product, categoryName, clientId);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific product by ID within client's tenant boundary.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId, Integer clientId) {
        log.debug("Fetching product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> {
                    log.warn("Product {} not found for client {}", productId, clientId);
                    return new ProductNotFoundException(productId, clientId);
                });
        
        Category category = categoryRepository.findByIdAndClientId(
                product.getCategoryId(), clientId).orElse(null);
        String categoryName = category != null ? category.getName() : "Unknown";
        
        return buildProductResponse(product, categoryName, clientId);
    }
    
    /**
     * Update an existing product with strict validation (atomic operation).
     * 
     * Update Rules:
     * - Can update: name, description, category_id, attributes
     * - Cannot update: client_id
     * - Attribute update is full replace (delete old, insert new)
     * - Same validations as create apply
     */
    @Transactional
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request, Integer clientId) {
        log.info("Updating product {} for client {}", productId, clientId);
        
        // 1. Find existing product
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException(productId, clientId));
        
        // 2. Validate new category
        Category category = validateCategory(request.getCategoryId(), clientId);
        
        String newName = request.getName().trim();
        
        // 3. Validate name uniqueness (excluding current product)
        if (productRepository.existsByClientIdAndCategoryIdAndNameIgnoreCaseExcluding(
                clientId, request.getCategoryId(), newName, productId)) {
            log.warn("Product update failed - name '{}' already exists in category {}", 
                    newName, request.getCategoryId());
            throw new IllegalArgumentException(
                String.format("Product '%s' already exists in this category", newName)
            );
        }
        
        // 4. Update product fields
        product.setName(newName);
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        
        Product updatedProduct = productRepository.save(product);
        
        log.info("Successfully updated product {} for client {}", productId, clientId);
        
        // 5. Replace attributes (atomic)
        productAttributeRepository.deleteByProductIdAndClientId(productId, clientId);
        
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            assignProductAttributes(productId, request.getCategoryId(), 
                    request.getAttributes(), clientId);
        }
        
        // 6. Return updated product response
        return buildProductResponse(updatedProduct, category.getName(), clientId);
    }
    
    /**
     * Enable (activate) a product
     */
    @Transactional
    public ProductResponse enableProduct(Integer productId, Integer clientId) {
        log.info("Enabling product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException(productId, clientId));
        
        if (product.getEnabled()) {
            log.info("Product {} is already enabled", productId);
        } else {
            product.setEnabled(true);
            productRepository.save(product);
            log.info("Successfully enabled product {} for client {}", productId, clientId);
        }
        
        Category category = categoryRepository.findByIdAndClientId(
                product.getCategoryId(), clientId).orElse(null);
        String categoryName = category != null ? category.getName() : "Unknown";
        
        return buildProductResponse(product, categoryName, clientId);
    }
    
    /**
     * Disable (soft delete) a product
     */
    @Transactional
    public ProductResponse disableProduct(Integer productId, Integer clientId) {
        log.info("Disabling product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException(productId, clientId));
        
        if (!product.getEnabled()) {
            log.info("Product {} is already disabled", productId);
        } else {
            product.setEnabled(false);
            productRepository.save(product);
            log.info("Successfully disabled product {} for client {}", productId, clientId);
        }
        
        Category category = categoryRepository.findByIdAndClientId(
                product.getCategoryId(), clientId).orElse(null);
        String categoryName = category != null ? category.getName() : "Unknown";
        
        return buildProductResponse(product, categoryName, clientId);
    }
    
    /**
     * Validate category exists, is enabled, and belongs to client
     */
    private Category validateCategory(Integer categoryId, Integer clientId) {
        Category category = categoryRepository.findByIdAndClientId(categoryId, clientId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, clientId));
        
        if (!category.getEnabled()) {
            throw new IllegalArgumentException(
                String.format("Category %d is disabled and cannot have products", categoryId)
            );
        }
        
        return category;
    }
    
    /**
     * Assign attributes to product with STRICT validation.
     * 
     * CRITICAL VALIDATION:
     * 1. Attribute must be assigned to category
     * 2. Attribute value must belong to attribute
     * 3. Attribute value must be enabled
     * 4. Attribute value must be assigned to category
     */
    private void assignProductAttributes(Integer productId, Integer categoryId, 
                                        Map<Integer, Integer> attributes, Integer clientId) {
        
        log.debug("Assigning {} attributes to product {}", attributes.size(), productId);
        
        List<ProductAttribute> productAttributes = new ArrayList<>();
        
        for (Map.Entry<Integer, Integer> entry : attributes.entrySet()) {
            Integer attributeId = entry.getKey();
            Integer attributeValueId = entry.getValue();
            
            // 1. Validate attribute is assigned to category
            boolean attributeAssigned = categoryAttributeRepository
                    .existsByCategoryIdAndAttributeIdAndClientId(categoryId, attributeId, clientId);
            
            if (!attributeAssigned) {
                throw new IllegalArgumentException(
                    String.format("Attribute %d is not assigned to category %d", attributeId, categoryId)
                );
            }
            
            // 2. Validate attribute value exists and belongs to client
            AttributeValue attributeValue = attributeValueRepository
                    .findByIdAndClientId(attributeValueId, clientId)
                    .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Attribute value %d not found", attributeValueId)
                    ));
            
            // 3. Validate attribute value belongs to attribute
            if (!attributeValue.getAttributeId().equals(attributeId)) {
                throw new IllegalArgumentException(
                    String.format("Attribute value %d does not belong to attribute %d", 
                            attributeValueId, attributeId)
                );
            }
            
            // 4. Validate attribute value is enabled
            if (!attributeValue.getEnabled()) {
                throw new IllegalArgumentException(
                    String.format("Attribute value %d is disabled", attributeValueId)
                );
            }
            
            // 5. Validate attribute value is assigned to category
            boolean valueAssigned = categoryAttributeValueRepository
                    .existsByCategoryIdAndAttributeValueIdAndClientId(
                            categoryId, attributeValueId, clientId);
            
            if (!valueAssigned) {
                throw new IllegalArgumentException(
                    String.format("Attribute value %d is not assigned to category %d", 
                            attributeValueId, categoryId)
                );
            }
            
            // Create product attribute mapping
            ProductAttribute productAttribute = new ProductAttribute(
                clientId, productId, attributeId, attributeValueId
            );
            productAttributes.add(productAttribute);
        }
        
        // Save all product attributes
        if (!productAttributes.isEmpty()) {
            productAttributeRepository.saveAll(productAttributes);
            log.debug("Successfully assigned {} attributes to product {}", 
                    productAttributes.size(), productId);
        }
    }
    
    /**
     * Build ProductResponse with attributes
     */
    private ProductResponse buildProductResponse(Product product, String categoryName, Integer clientId) {
        ProductResponse response = ProductResponse.fromEntity(product, categoryName);
        
        // Fetch product attributes
        List<ProductAttribute> productAttributes = productAttributeRepository
                .findAllByProductIdAndClientId(product.getId(), clientId);
        
        List<ProductAttributeResponse> attributeResponses = new ArrayList<>();
        
        for (ProductAttribute pa : productAttributes) {
            // Get attribute name
            Attribute attribute = attributeRepository.findByIdAndClientId(pa.getAttributeId(), clientId)
                    .orElse(null);
            String attributeName = attribute != null ? attribute.getName() : "Unknown";
            
            // Get attribute value
            AttributeValue attributeValue = attributeValueRepository
                    .findByIdAndClientId(pa.getAttributeValueId(), clientId)
                    .orElse(null);
            String value = attributeValue != null ? attributeValue.getValue() : "Unknown";
            
            ProductAttributeResponse attrResponse = new ProductAttributeResponse(
                pa.getAttributeId(),
                attributeName,
                pa.getAttributeValueId(),
                value
            );
            attributeResponses.add(attrResponse);
        }
        
        response.setAttributes(attributeResponses);
        return response;
    }
}
