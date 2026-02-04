package com.elowen.identity.repository;

import com.elowen.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE (u.username = :identifier OR u.emailId = :identifier) AND u.clientId = :clientId")
    Optional<User> findByUsernameOrEmailAndClientId(@Param("identifier") String identifier, @Param("clientId") Integer clientId);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndClientId(String username, Integer clientId);
}
