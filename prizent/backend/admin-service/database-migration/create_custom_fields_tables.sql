-- Custom Fields Configuration Table
CREATE TABLE IF NOT EXISTS p_custom_fields_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    module CHAR(1) NOT NULL COMMENT 'p=product, m=marketplace, b=brand, c=category',
    field_type VARCHAR(50) NOT NULL COMMENT 'text, numeric, dropdown, date, file',
    required BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BIGINT,
    create_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_module (client_id, module),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Custom Fields Values Table
CREATE TABLE IF NOT EXISTS p_custom_fields_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    custom_field_id BIGINT NOT NULL,
    client_id INT NOT NULL,
    module_id BIGINT NOT NULL COMMENT 'ID of product/brand/category/marketplace',
    module CHAR(1) NOT NULL COMMENT 'p=product, m=marketplace, b=brand, c=category',
    value TEXT,
    create_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    INDEX idx_client_module_id (client_id, module, module_id),
    INDEX idx_custom_field (custom_field_id),
    FOREIGN KEY (custom_field_id) REFERENCES p_custom_fields_configuration(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
