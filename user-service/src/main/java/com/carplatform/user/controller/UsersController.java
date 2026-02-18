package com.carplatform.user.controller;

import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.service.UserService;
import com.carplatform.user.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for User Management APIs (Minimal Scope)
 * 
 * Limited endpoints for identity and roles, no authentication.
 * Exposes only: POST /users (create) and GET /users/{userId} (retrieve)
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    /**
     * Create a new user (registration endpoint)
     * 
     * @param request RegisterUserRequest with name, email, phone
     * @return 201 Created with new user details
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(request));
    }

    /**
     * Get user details by ID
     * 
     * @param userId the user ID (UUID)
     * @return 200 OK with user details, 404 if not found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
