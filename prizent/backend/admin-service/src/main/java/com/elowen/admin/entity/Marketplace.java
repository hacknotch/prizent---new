package com.elowen.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "p_marketplaces")
public class Marketplace {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @OneToMany(mappedBy = "marketplace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MarketplaceCost> costs;
    
    // Constructors
    public Marketplace() {}
    
    public Marketplace(Integer clientId, String name, String description, Boolean enabled) {
        this.clientId = clientId;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createDateTime = LocalDateTime.now();
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
    
    public List<MarketplaceCost> getCosts() {
        return costs;
    }
    
    public void setCosts(List<MarketplaceCost> costs) {
        this.costs = costs;
    }
}