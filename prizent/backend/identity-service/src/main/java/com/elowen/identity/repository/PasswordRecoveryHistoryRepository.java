package com.elowen.identity.repository;

import com.elowen.identity.entity.PasswordRecoveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRecoveryHistoryRepository extends JpaRepository<PasswordRecoveryHistory, Integer> {

    /**
     * Find all password history for a specific user within a client (tenant-safe)
     */
    @Query("SELECT p FROM PasswordRecoveryHistory p WHERE p.clientId = :clientId AND p.userId = :userId " +
           "ORDER BY p.changedTimeDate DESC")
    List<PasswordRecoveryHistory> findByClientIdAndUserId(@Param("clientId") Integer clientId, 
                                                           @Param("userId") Long userId);

    /**
     * Find last N password changes for a user (for password reuse prevention)
     */
    @Query(value = "SELECT * FROM p_password_recovery_histories WHERE client_id = :clientId AND user_id = :userId " +
           "ORDER BY changed_time_date DESC LIMIT :limit", nativeQuery = true)
    List<PasswordRecoveryHistory> findLastNPasswordsByClientIdAndUserId(@Param("clientId") Integer clientId, 
                                                                        @Param("userId") Long userId, 
                                                                        @Param("limit") int limit);

    /**
     * Count password changes for a user
     */
    @Query("SELECT COUNT(p) FROM PasswordRecoveryHistory p WHERE p.clientId = :clientId AND p.userId = :userId")
    long countByClientIdAndUserId(@Param("clientId") Integer clientId, @Param("userId") Long userId);
}
