package com.elowen.admin.repository;

import com.elowen.admin.entity.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    
    List<CustomFieldValue> findAllByClientIdAndModuleAndModuleId(Integer clientId, String module, Long moduleId);
    
    List<CustomFieldValue> findAllByClientIdAndModule(Integer clientId, String module);
}
