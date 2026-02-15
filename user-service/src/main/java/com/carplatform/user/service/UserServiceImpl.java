package com.carplatform.user.service;

import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.model.User;
import com.carplatform.user.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-Memory Implementation of User Service
 * 
 * Uses a HashMap to store users for Phase 3 (no database).
 * Manages user registration, profiles, roles, and soft deletes.
 */
@Service
public class UserServiceImpl implements UserService {

    private final Map<UUID, User> userRepository = new HashMap<>();

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

        userRepository.put(user.userId(), user);
        return mapToResponse(user);
    }

    @Override
    public Optional<UserResponse> getUserById(UUID userId) {
        return Optional.ofNullable(userRepository.get(userId))
                .map(this::mapToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.values()
                .stream()
                .filter(user -> user.email().equals(email))
                .findFirst()
                .map(this::mapToResponse);
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.values()
                .stream()
                .filter(user -> user.active())
                .filter(user -> user.role() == role)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> listAllActiveUsers() {
        return userRepository.values()
                .stream()
                .filter(User::active)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUserProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        User updatedUser = user.updateDetails(
                request.name() != null ? request.name() : user.name(),
                request.phone() != null ? request.phone() : user.phone());

        userRepository.put(userId, updatedUser);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserRole(UUID userId, UserRole newRole) {
        User user = userRepository.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        if (!user.active()) {
            throw new RuntimeException("Cannot update role of inactive user");
        }

        User updatedUser = user.withRole(newRole);
        userRepository.put(userId, updatedUser);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse deactivateUser(UUID userId) {
        User user = userRepository.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        User deactivatedUser = user.deactivate();
        userRepository.put(userId, deactivatedUser);
        return mapToResponse(deactivatedUser);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.values()
                .stream()
                .anyMatch(user -> user.email().equals(email) && user.active());
    }

    /**
     * Convert User model to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.userId(),
                user.name(),
                user.email(),
                user.phone(),
                user.role(),
                user.active(),
                user.createdAt(),
                user.lastUpdated());
    }
}
