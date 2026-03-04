package com.elowen.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Entity for multi-tenant product management
 * Follows strict architectural rules with BIGINT/INT IDs only
 */
@Entity
@Table(name = "p_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Client ID is required")
    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 100)
    @Column(name = "product_number", length = 100)
    private String productNumber;

    @Size(max = 100)
    @Column(name = "style_code", length = 100)
    private String styleCode;

    /**
     * Brand ID reference - validation deferred to external admin-service
     * Cross-service validation not allowed per architectural rules
     * Ensure brand exists in admin-service before product creation
     */
    @NotNull(message = "Brand ID is required")
    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @NotBlank(message = "SKU code is required")
    @Size(max = 100, message = "SKU code must not exceed 100 characters")
    @Column(name = "sku_code", nullable = false, length = 100)
    private String skuCode;

    /**
     * Category ID reference - validation deferred to external admin-service  
     * Cross-service validation not allowed per architectural rules
     * Ensure category exists in admin-service before product creation
     */
    @NotNull(message = "Category ID is required")
    @Column(name = "category_id", nullable = false) 
    private Long categoryId;

    @DecimalMin(value = "0.0", inclusive = true, message = "MRP must be greater than or equal to 0")
    @Column(name = "mrp", precision = 12, scale = 2)
    private BigDecimal mrp = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Product cost must be greater than or equal to 0")
    @Column(name = "product_cost", precision = 12, scale = 2)
    private BigDecimal productCost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Proposed selling price (sales) must be greater than or equal to 0")
    @Column(name = "proposed_selling_price_sales", precision = 12, scale = 2)
    private BigDecimal proposedSellingPriceSales = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Proposed selling price (non-sales) must be greater than or equal to 0")
    @Column(name = "proposed_selling_price_non_sales", precision = 12, scale = 2)
    private BigDecimal proposedSellingPriceNonSales = BigDecimal.ZERO;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;

    @Column(name = "updated_by")
    private Long updatedBy;

    // Constructors
    public Product() {}

    public Product(Integer clientId, String name, Long brandId, String skuCode, 
                   Long categoryId, BigDecimal mrp, BigDecimal productCost,
                   BigDecimal proposedSellingPriceSales, BigDecimal proposedSellingPriceNonSales,
                   Long updatedBy) {
        this.clientId = clientId;
        this.name = name;
        this.brandId = brandId;
        this.skuCode = skuCode;
        this.categoryId = categoryId;
        this.mrp = mrp != null ? mrp : BigDecimal.ZERO;
        this.productCost = productCost != null ? productCost : BigDecimal.ZERO;
        this.proposedSellingPriceSales = proposedSellingPriceSales != null ? proposedSellingPriceSales : BigDecimal.ZERO;
        this.proposedSellingPriceNonSales = proposedSellingPriceNonSales != null ? proposedSellingPriceNonSales : BigDecimal.ZERO;
        this.updatedBy = updatedBy;
        this.enabled = true;
    }

    @PrePersist
    protected void onCreate() {
        createDateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp != null ? mrp : BigDecimal.ZERO;
    }

    public BigDecimal getProductCost() {
        return productCost;
    }

    public void setProductCost(BigDecimal productCost) {
        this.productCost = productCost != null ? productCost : BigDecimal.ZERO;
    }

    public BigDecimal getProposedSellingPriceSales() {
        return proposedSellingPriceSales;
    }

    public void setProposedSellingPriceSales(BigDecimal proposedSellingPriceSales) {
        this.proposedSellingPriceSales = proposedSellingPriceSales != null ? proposedSellingPriceSales : BigDecimal.ZERO;
    }

    public BigDecimal getProposedSellingPriceNonSales() {
        return proposedSellingPriceNonSales;
    }

    public void setProposedSellingPriceNonSales(BigDecimal proposedSellingPriceNonSales) {
        this.proposedSellingPriceNonSales = proposedSellingPriceNonSales != null ? proposedSellingPriceNonSales : BigDecimal.ZERO;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled != null ? enabled : true;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", name='" + name + '\'' +
                ", skuCode='" + skuCode + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}