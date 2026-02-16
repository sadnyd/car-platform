package com.carplatform.user.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * User domain model representing a car platform customer or admin.
 * JPA-managed entity for persistence.
 * Minimal fields for Phase 4 - security-adjacent design.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_user_email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    // Constructors
    public User() {
    }

    public User(UUID userId, String name, String email, String phone, UserRole role,
            boolean active, Instant createdAt, Instant lastUpdated) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Factory method to create a new customer user with CUSTOMER role.
     */
    public static User createCustomer(String name, String email, String phone) {
        User user = new User();
        // Don't set userId - let Hibernate/JPA generate it with @GeneratedValue
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setLastUpdated(Instant.now());
        return user;
    }

    /**
     * Factory method to create a new admin user.
     */
    public static User createAdmin(String name, String email, String phone) {
        User user = new User();
        // Don't set userId - let Hibernate/JPA generate it with @GeneratedValue
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setLastUpdated(Instant.now());
        return user;
    }

    /**
     * Deactivate a user.
     */
    public User deactivate() {
        this.active = false;
        this.lastUpdated = Instant.now();
        return this;
    }

    /**
     * Update user details.
     */
    public User updateDetails(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.lastUpdated = Instant.now();
        return this;
    }

    /**
     * Update role.
     */
    public User withRole(UserRole newRole) {
        this.role = newRole;
        this.lastUpdated = Instant.now();
        return this;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
