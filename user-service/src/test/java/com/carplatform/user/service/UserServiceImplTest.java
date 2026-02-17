package com.carplatform.user.service;

import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.dto.UserResponse;
import com.carplatform.user.model.User;
import com.carplatform.user.model.UserRole;
import com.carplatform.user.repository.UserRepository;
import com.carplatform.user.testdata.UserTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Business Rule Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUserShouldFailWhenEmailAlreadyExists() {
        RegisterUserRequest request = UserTestDataFactory.validRegisterRequest();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(UserTestDataFactory.activeCustomer()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(request));

        assertTrue(exception.getMessage().contains("Email already registered"));
    }

    @Test
    void registerUserShouldPersistCustomer() {
        RegisterUserRequest request = UserTestDataFactory.validRegisterRequest();
        User saved = UserTestDataFactory.activeCustomer();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.registerUser(request);

        assertEquals(UserRole.CUSTOMER, response.role());
        assertTrue(response.active());
    }

    @Test
    void updateUserRoleShouldFailForInactiveUser() {
        User inactive = UserTestDataFactory.inactiveCustomer();
        when(userRepository.findById(inactive.getUserId())).thenReturn(Optional.of(inactive));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUserRole(inactive.getUserId(), UserRole.ADMIN));

        assertTrue(exception.getMessage().contains("inactive user"));
    }

    @Test
    void listAllActiveUsersShouldFilterInactive() {
        when(userRepository.findAll())
                .thenReturn(List.of(UserTestDataFactory.activeCustomer(), UserTestDataFactory.inactiveCustomer()));

        List<UserResponse> users = userService.listAllActiveUsers();

        assertEquals(1, users.size());
        assertTrue(users.get(0).active());
    }

    @Test
    void updateUserProfileShouldApplyNonNullFields() {
        User user = UserTestDataFactory.activeCustomer();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserProfile(user.getUserId(),
                new UpdateUserRequest("Alice Updated", null));

        assertEquals("Alice Updated", response.name());
        assertEquals("+14155552671", response.phone());
    }
}
