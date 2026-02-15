package com.carplatform.user.controller;

import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.model.UserRole;
import com.carplatform.user.service.UserService;
import com.carplatform.user.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listActive() {
        return ResponseEntity.ok(userService.listAllActiveUsers());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable UUID userId,
            @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.updateUserRole(userId, role));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserResponse> deactivate(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.deactivateUser(userId));
    }
}
