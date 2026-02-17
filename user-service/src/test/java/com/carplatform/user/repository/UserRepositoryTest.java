package com.carplatform.user.repository;

import com.carplatform.user.model.User;
import com.carplatform.user.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository JPA Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        User user = User.createCustomer("Alice", "alice@example.com", "+14155552671");
        userRepository.save(user);

        assertTrue(userRepository.findByEmail("alice@example.com").isPresent());
    }

    @Test
    void shouldReturnEmptyWhenEmailMissing() {
        assertTrue(userRepository.findByEmail("missing@example.com").isEmpty());
    }

    @Test
    void shouldPersistRoleCorrectly() {
        User user = User.createAdmin("Admin", "admin@example.com", "+14155550000");
        userRepository.save(user);

        User persisted = userRepository.findByEmail("admin@example.com").orElseThrow();
        assertEquals(UserRole.ADMIN, persisted.getRole());
    }
}
