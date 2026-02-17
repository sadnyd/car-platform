package com.carplatform.user.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Domain Unit Tests")
class UserUnitTest {

    @Test
    void createCustomerShouldInitializeCustomerRoleAndActive() {
        User user = User.createCustomer("Alice", "alice@example.com", "+14155552671");

        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void createAdminShouldSetAdminRole() {
        User user = User.createAdmin("Admin", "admin@example.com", "+14155552671");

        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void deactivateShouldSetInactive() {
        User user = User.createCustomer("Alice", "alice@example.com", "+14155552671");

        user.deactivate();

        assertFalse(user.isActive());
    }

    @Test
    void updateDetailsShouldReplaceMutableFields() {
        User user = User.createCustomer("Alice", "alice@example.com", "+14155552671");

        user.updateDetails("Alice B", "+14155552672");

        assertEquals("Alice B", user.getName());
        assertEquals("+14155552672", user.getPhone());
    }
}
