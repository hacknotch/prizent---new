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
 */
@Service
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, Integer clientId) {
        log.info("Creating product for client {}: {}", clientId, request.getName());
        
        // Validate category exists
        Category category = categoryRepository.findByIdAndClientId(request.getCategoryId(), clientId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        
        if (!category.getEnabled()) {
            throw new IllegalArgumentException("Cannot create product under disabled category");
        }
        
        // Create product
        Product product = new Product();
        product.setClientId(clientId);
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setEnabled(true);
        
        Product savedProduct = productRepository.save(product);
        
        return ProductResponse.fromEntity(savedProduct, category.getName());
    }
    
    public List<ProductResponse> getAllProducts(Integer clientId) {
        log.info("Fetching all products for client {}", clientId);
        
        List<Product> products = productRepository.findAllByClientIdOrderByCreateDateTimeDesc(clientId);
        
        return products.stream()
                .map(product -> {
                    String categoryName = categoryRepository.findById(product.getCategoryId())
                            .map(Category::getName)
                            .orElse("Unknown");
                    return ProductResponse.fromEntity(product, categoryName);
                })
                .collect(Collectors.toList());
    }
    
    public ProductResponse getProductById(Integer productId, Integer clientId) {
        log.info("Fetching product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        
        return ProductResponse.fromEntity(product, categoryName);
    }
    
    @Transactional
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request, Integer clientId) {
        log.info("Updating product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        // Validate category if changed
        if (!request.getCategoryId().equals(product.getCategoryId())) {
            Category category = categoryRepository.findByIdAndClientId(request.getCategoryId(), clientId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            
            if (!category.getEnabled()) {
                throw new IllegalArgumentException("Cannot move product to disabled category");
            }
        }
        
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        
        Product updatedProduct = productRepository.save(product);
        
        String categoryName = categoryRepository.findById(updatedProduct.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        
        return ProductResponse.fromEntity(updatedProduct, categoryName);
    }
    
    @Transactional
    public ProductResponse enableProduct(Integer productId, Integer clientId) {
        log.info("Enabling product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        product.setEnabled(true);
        Product updatedProduct = productRepository.save(product);
        
        String categoryName = categoryRepository.findById(updatedProduct.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        
        return ProductResponse.fromEntity(updatedProduct, categoryName);
    }
    
    @Transactional
    public ProductResponse disableProduct(Integer productId, Integer clientId) {
        log.info("Disabling product {} for client {}", productId, clientId);
        
        Product product = productRepository.findByIdAndClientId(productId, clientId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        product.setEnabled(false);
        Product updatedProduct = productRepository.save(product);
        
        String categoryName = categoryRepository.findById(updatedProduct.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        
        return ProductResponse.fromEntity(updatedProduct, categoryName);
    }
}
