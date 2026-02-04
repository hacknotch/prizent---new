package com.elowen.identity.repository;

import com.elowen.identity.entity.LoginLogoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginLogoutHistoryRepository extends JpaRepository<LoginLogoutHistory, Long> {

    /**
     * Find all login histories for a specific client (tenant-safe)
     */
    @Query("SELECT h FROM LoginLogoutHistory h WHERE h.clientId = :clientId ORDER BY h.loginDateTime DESC")
    List<LoginLogoutHistory> findAllByClientId(@Param("clientId") Integer clientId);

    /**
     * Find all login histories for a specific user within a client
     */
    @Query("SELECT h FROM LoginLogoutHistory h WHERE h.clientId = :clientId AND h.userId = :userId ORDER BY h.loginDateTime DESC")
    List<LoginLogoutHistory> findByClientIdAndUserId(@Param("clientId") Integer clientId, @Param("userId") Long userId);

    /**
     * Find the latest login record for a user (used for logout)
     */
    @Query("SELECT h FROM LoginLogoutHistory h WHERE h.clientId = :clientId AND h.userId = :userId " +
           "AND h.logoutDateTime IS NULL ORDER BY h.loginDateTime DESC")
    Optional<LoginLogoutHistory> findLatestActiveLoginByClientIdAndUserId(@Param("clientId") Integer clientId, @Param("userId") Long userId);

    /**
     * Update logout time for a specific login record
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginLogoutHistory h SET h.logoutDateTime = :logoutDateTime WHERE h.id = :id")
    void updateLogoutTime(@Param("id") Long id, @Param("logoutDateTime") LocalDateTime logoutDateTime);

    /**
     * Find all active sessions (no logout time) for a client
     */
    @Query("SELECT h FROM LoginLogoutHistory h WHERE h.clientId = :clientId AND h.logoutDateTime IS NULL ORDER BY h.loginDateTime DESC")
    List<LoginLogoutHistory> findActiveSessionsByClientId(@Param("clientId") Integer clientId);

    /**
     * Count total logins for a user within a client
     */
    @Query("SELECT COUNT(h) FROM LoginLogoutHistory h WHERE h.clientId = :clientId AND h.userId = :userId")
    long countLoginsByClientIdAndUserId(@Param("clientId") Integer clientId, @Param("userId") Long userId);

    /**
     * Find login histories within a date range for a client
     */
    @Query("SELECT h FROM LoginLogoutHistory h WHERE h.clientId = :clientId " +
           "AND h.loginDateTime >= :startDate AND h.loginDateTime <= :endDate " +
           "ORDER BY h.loginDateTime DESC")
    List<LoginLogoutHistory> findByClientIdAndDateRange(@Param("clientId") Integer clientId, 
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
}
