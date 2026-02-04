package com.elowen.admin.repository;

import com.elowen.admin.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    List<Brand> findByClientId(Integer clientId);
    
    Optional<Brand> findByIdAndClientId(Long id, Integer clientId);
    
    Optional<Brand> findByNameAndClientId(String name, Integer clientId);
    
    List<Brand> findByClientIdOrderByCreateDateTimeDesc(Integer clientId);
}
