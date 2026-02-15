-- Migration Script: Create Marketplace Configuration Tables
-- Execute this in the MySQL database

-- Create p_marketplaces table
CREATE TABLE IF NOT EXISTS p_marketplaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    INDEX idx_marketplaces_client_id (client_id),
    INDEX idx_marketplaces_client_enabled (client_id, enabled),
    UNIQUE KEY uk_marketplaces_client_name (client_id, name)
);

-- Create p_marketplace_costs table
CREATE TABLE IF NOT EXISTS p_marketplace_costs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    marketplace_id BIGINT NOT NULL,
    cost_category VARCHAR(20) NOT NULL COMMENT 'COMMISSION / SHIPPING / MARKETING',
    cost_value_type CHAR(1) NOT NULL COMMENT 'a (amount) or p (percentage)',
    cost_value DECIMAL(12,2) NOT NULL,
    cost_product_range VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    INDEX idx_marketplace_costs_client_id (client_id),
    INDEX idx_marketplace_costs_marketplace_id (marketplace_id),
    INDEX idx_marketplace_costs_client_marketplace (client_id, marketplace_id),
    
    FOREIGN KEY (marketplace_id) REFERENCES p_marketplaces(id) ON DELETE CASCADE,
    
    CHECK (cost_category IN ('COMMISSION', 'SHIPPING', 'MARKETING')),
    CHECK (cost_value_type IN ('A', 'P')),
    CHECK (cost_value >= 0)
);