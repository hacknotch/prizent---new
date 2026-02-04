package com.elowen.admin.repository;

import com.elowen.admin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    Optional<Category> findByIdAndClientId(Integer id, Integer clientId);
    
    List<Category> findAllByClientIdOrderByCreateDateTimeDesc(Integer clientId);
    
    List<Category> findAllByClientIdAndParentCategoryId(Integer clientId, Integer parentCategoryId);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
           "WHERE c.clientId = :clientId " +
           "AND c.name = :name " +
           "AND (:parentCategoryId IS NULL AND c.parentCategoryId IS NULL OR c.parentCategoryId = :parentCategoryId)")
    boolean existsByClientIdAndNameAndParentCategoryId(
        @Param("clientId") Integer clientId,
        @Param("name") String name,
        @Param("parentCategoryId") Integer parentCategoryId
    );
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
           "WHERE c.clientId = :clientId " +
           "AND c.name = :name " +
           "AND (:parentCategoryId IS NULL AND c.parentCategoryId IS NULL OR c.parentCategoryId = :parentCategoryId) " +
           "AND c.id != :excludeId")
    boolean existsByClientIdAndNameAndParentCategoryIdExcluding(
        @Param("clientId") Integer clientId,
        @Param("name") String name,
        @Param("parentCategoryId") Integer parentCategoryId,
        @Param("excludeId") Integer excludeId
    );
}
