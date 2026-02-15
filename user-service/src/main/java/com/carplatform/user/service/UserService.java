package com.carplatform.user.service;

import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.model.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service Interface
 * 
 * Defines the contract for user management operations.
 * Handles user registration, profile management, and role assignment.
 */
public interface UserService {

    /**
     * Register a new user
     */
    UserResponse registerUser(RegisterUserRequest request);

    /**
     * Get user by ID
     */
    Optional<UserResponse> getUserById(UUID userId);

    /**
     * Get user by email
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * Get users by role
     */
    List<UserResponse> getUsersByRole(UserRole role);

    /**
     * List all active users
     */
    List<UserResponse> listAllActiveUsers();

    /**
     * Update user profile
     */
    UserResponse updateUserProfile(UUID userId, UpdateUserRequest request);

    /**
     * Update user role
     */
    UserResponse updateUserRole(UUID userId, UserRole newRole);

    /**
     * Deactivate user (soft delete)
     */
    UserResponse deactivateUser(UUID userId);

    /**
     * Check if user email exists
     */
    boolean emailExists(String email);
}
