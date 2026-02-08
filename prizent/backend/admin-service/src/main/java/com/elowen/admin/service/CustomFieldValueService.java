package com.elowen.admin.service;

import com.elowen.admin.dto.CustomFieldValueResponse;
import com.elowen.admin.dto.SaveCustomFieldValueRequest;
import com.elowen.admin.entity.CustomFieldConfiguration;
import com.elowen.admin.entity.CustomFieldValue;
import com.elowen.admin.exception.CustomFieldNotFoundException;
import com.elowen.admin.exception.InvalidCustomFieldValueException;
import com.elowen.admin.repository.CustomFieldConfigurationRepository;
import com.elowen.admin.repository.CustomFieldValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomFieldValueService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomFieldValueService.class);
    
    private final CustomFieldValueRepository customFieldValueRepository;
    private final CustomFieldConfigurationRepository customFieldConfigRepository;
    
    @Autowired
    public CustomFieldValueService(CustomFieldValueRepository customFieldValueRepository,
                                    CustomFieldConfigurationRepository customFieldConfigRepository) {
        this.customFieldValueRepository = customFieldValueRepository;
        this.customFieldConfigRepository = customFieldConfigRepository;
    }
    
    /**
     * Save custom field values
     * Business Rules:
     * - Disabled fields cannot accept values
     * - Required fields must have a value
     * - Validate value based on field_type
     */
    @Transactional
    public CustomFieldValueResponse saveCustomFieldValue(SaveCustomFieldValueRequest request, 
                                                          Integer clientId, Long createdBy) {
        Long customFieldId = request.getCustomFieldId();
        Long moduleId = request.getModuleId();
        String module = request.getModule();
        String value = request.getValue();
        
        // Get the specific custom field configuration
        CustomFieldConfiguration fieldConfig = customFieldConfigRepository
                .findByIdAndClientId(customFieldId, clientId)
                .orElseThrow(() -> new CustomFieldNotFoundException(customFieldId, clientId));
        
        // Validate the module matches
        if (!fieldConfig.getModule().equals(module)) {
            throw new InvalidCustomFieldValueException(
                String.format("Custom field ID %d belongs to module '%s', not '%s'", 
                    customFieldId, fieldConfig.getModule(), module));
        }
        
        // Validate against the specific field configuration
        validateCustomFieldValue(fieldConfig, value);
        
        // Create and save the value
        CustomFieldValue customFieldValue = new CustomFieldValue(
            customFieldId,
            clientId,
            moduleId,
            module,
            value,
            createdBy
        );
        
        CustomFieldValue savedValue = customFieldValueRepository.save(customFieldValue);
        
        log.info("Saved custom field value ID {} for client {} module {} moduleId {}", 
                savedValue.getId(), clientId, module, moduleId);
        return CustomFieldValueResponse.fromEntity(savedValue);
    }
    
    /**
     * Get all custom field values by module and moduleId
     */
    @Transactional(readOnly = true)
    public List<CustomFieldValueResponse> getCustomFieldValues(Integer clientId, String module, Long moduleId) {
        List<CustomFieldValue> values = customFieldValueRepository
                .findAllByClientIdAndModuleAndModuleId(clientId, module, moduleId);
        
        return values.stream()
                .map(CustomFieldValueResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all custom field values by module (without moduleId filter)
     */
    @Transactional(readOnly = true)
    public List<CustomFieldValueResponse> getCustomFieldValuesByModule(Integer clientId, String module) {
        List<CustomFieldValue> values = customFieldValueRepository
                .findAllByClientIdAndModule(clientId, module);
        
        return values.stream()
                .map(CustomFieldValueResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Validate custom field value against specific configuration
     */
    private void validateCustomFieldValue(CustomFieldConfiguration config, String value) {
        // Rule: Disabled fields cannot accept values
        if (!config.getEnabled()) {
            throw new InvalidCustomFieldValueException(
                String.format("Custom field '%s' is disabled and cannot accept values", config.getName()));
        }
        
        // Rule: Required fields must have a value
        if (config.getRequired() && (value == null || value.trim().isEmpty())) {
            throw new InvalidCustomFieldValueException(
                String.format("Custom field '%s' is required and must have a value", config.getName()));
        }
        
        // Rule: Validate value based on field_type
        if (value != null && !value.trim().isEmpty()) {
            validateValueByFieldType(config.getFieldType(), value, config.getName());
        }
    }
    
    /**
     * Get custom field values for a specific brand
     * Convenience method for brand-specific custom field consumption
     */
    @Transactional(readOnly = true)
    public List<CustomFieldValueResponse> getBrandCustomFieldValues(Integer clientId, Long brandId) {
        return getCustomFieldValues(clientId, "b", brandId);
    }
    
    /**
     * Validate value based on field type
     */
    private void validateValueByFieldType(String fieldType, String value, String fieldName) {
        switch (fieldType) {
            case "numeric":
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new InvalidCustomFieldValueException(
                        String.format("Field '%s' expects a numeric value, but got: %s", fieldName, value));
                }
                break;
            
            case "date":
                try {
                    LocalDate.parse(value);
                } catch (DateTimeParseException e) {
                    throw new InvalidCustomFieldValueException(
                        String.format("Field '%s' expects a date value in ISO format (YYYY-MM-DD), but got: %s", 
                            fieldName, value));
                }
                break;
            
            case "text":
            case "dropdown":
            case "file":
                // No specific validation for these types
                break;
            
            default:
                log.warn("Unknown field type: {}", fieldType);
        }
    }
}
