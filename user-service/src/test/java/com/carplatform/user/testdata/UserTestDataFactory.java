package com.carplatform.user.testdata;

import com.carplatform.user.dto.RegisterUserRequest;
import com.carplatform.user.dto.UpdateUserRequest;
import com.carplatform.user.model.User;
import com.carplatform.user.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public final class UserTestDataFactory {

    public static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private UserTestDataFactory() {
    }

    public static RegisterUserRequest validRegisterRequest() {
        return new RegisterUserRequest("Alice", "alice@example.com", "+14155552671");
    }

    public static UpdateUserRequest validUpdateRequest() {
        return new UpdateUserRequest("Alice Updated", "+14155552672");
    }

    public static User activeCustomer() {
        User user = new User();
        user.setUserId(USER_ID);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPhone("+14155552671");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(Instant.now().minusSeconds(120));
        user.setLastUpdated(Instant.now().minusSeconds(60));
        return user;
    }

    public static User inactiveCustomer() {
        User user = activeCustomer();
        user.setActive(false);
        return user;
    }
}
