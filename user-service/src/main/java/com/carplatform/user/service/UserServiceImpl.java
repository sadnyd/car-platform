package com.carplatform.user.service;

import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.model.User;
import com.carplatform.user.model.UserRole;
import com.carplatform.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed Implementation of User Service
 * 
 * Uses UserRepository (Spring Data JPA) to persist users in PostgreSQL.
 * Minimal scope - identity and roles, no authentication.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        // Check if email already exists
        if (emailExists(request.email())) {
            throw new RuntimeException("Email already registered: " + request.email());
        }

        User user = User.createCustomer(
                request.name(),
                request.email(),
                request.phone());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public Optional<UserResponse> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(this::mapToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToResponse);
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.isActive())
                .filter(user -> user.getRole() == role)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> listAllActiveUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUserProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.updateDetails(
                request.name() != null ? request.name() : user.getName(),
                request.phone() != null ? request.phone() : user.getPhone());
        user.setLastUpdated(Instant.now());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserRole(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (!user.isActive()) {
            throw new RuntimeException("Cannot update role of inactive user");
        }

        user.withRole(newRole);
        user.setLastUpdated(Instant.now());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.deactivate();
        User deactivatedUser = userRepository.save(user);
        return mapToResponse(deactivatedUser);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email)
                .map(User::isActive)
                .orElse(false);
    }

    /**
     * Convert User model to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getLastUpdated());
    }
}
