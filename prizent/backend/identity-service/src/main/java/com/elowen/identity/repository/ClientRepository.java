package com.elowen.identity.repository;

import com.elowen.identity.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    /**
     * Find client by domain and enabled status
     * This is the core method for domain-based client resolution
     */
    Optional<Client> findByClientDomainAndEnabled(String clientDomain, Boolean enabled);

    /**
     * Check if client domain exists (for validation)
     */
    boolean existsByClientDomain(String clientDomain);

    /**
     * Find by ID and enabled status
     */
    Optional<Client> findByIdAndEnabled(Integer id, Boolean enabled);

    /**
     * Find enabled client by ID
     */
    default Optional<Client> findEnabledById(Integer id) {
        return findByIdAndEnabled(id, true);
    }

    /**
     * Check if client exists and is enabled
     */
    boolean existsByIdAndEnabled(Integer id, Boolean enabled);

    /**
     * Check if enabled client exists
     */
    default boolean existsEnabledById(Integer id) {
        return existsByIdAndEnabled(id, true);
    }

    /**
     * Find client by name
     */
    Optional<Client> findByName(String name);

    /**
     * Check if name exists (for uniqueness validation)
     */
    boolean existsByName(String name);
}
