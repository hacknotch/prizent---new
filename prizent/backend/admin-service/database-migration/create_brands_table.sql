-- Create or fix the p_brands table
DROP TABLE IF EXISTS p_brands;

CREATE TABLE p_brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_id (client_id),
    INDEX idx_name (name),
    UNIQUE KEY uk_brand_name_client (name, client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
