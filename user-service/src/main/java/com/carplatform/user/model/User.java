package com.carplatform.user.model;

import java.time.Instant;
import java.util.UUID;

/**
 * User domain model representing a car platform customer or admin.
 * This is an immutable domain record.
 * 
 * No database annotations - this is a pure domain model.
 */
public record User(
        UUID userId,
        String name,
        String email,
        String phone,
        UserRole role,
        boolean active,
        Instant createdAt,
        Instant lastUpdated) {

    /**
     * Factory method to create a new customer user with CUSTOMER role.
     */
    public static User createCustomer(String name, String email, String phone) {
        return new User(
                UUID.randomUUID(),
                name,
                email,
                phone,
                UserRole.CUSTOMER,
                true,
                Instant.now(),
                Instant.now());
    }

    /**
     * Factory method to create a new admin user.
     */
    public static User createAdmin(String name, String email, String phone) {
        return new User(
                UUID.randomUUID(),
                name,
                email,
                phone,
                UserRole.ADMIN,
                true,
                Instant.now(),
                Instant.now());
    }

    /**
     * Deactivate a user.
     */
    public User deactivate() {
        return new User(
                this.userId,
                this.name,
                this.email,
                this.phone,
                this.role,
                false,
                this.createdAt,
                Instant.now());
    }

    /**
     * Update user details.
     */
    public User updateDetails(String name, String phone) {
        return new User(
                this.userId,
                name,
                this.email,
                phone,
                this.role,
                this.active,
                this.createdAt,
                Instant.now());
    }

    /**
     * Update role.
     */
    public User withRole(UserRole newRole) {
        return new User(
                this.userId,
                this.name,
                this.email,
                this.phone,
                newRole,
                this.active,
                this.createdAt,
                Instant.now());
    }
}
