package com.carplatform.user.repository;

import com.carplatform.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity persistence operations.
 * Extends JpaRepository to provide CRUD and query capabilities.
 * Minimal scope for Phase 4 - only identity queries, no authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email.
     * 
     * @param email the user's email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
}
