package com.assignment.bank.user.mapper;

import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void shouldMapUserRequestToEntityCorrectly() {

        UserRequest request = new UserRequest(
                "john",
                "12345678913",
                "john@email.com",
                "123456"
        );

        String passwordHash = "hashed-password";
        User user = userMapper.mapToEntity(request, passwordHash);

        assertNotNull(user);
        assertEquals("john", user.getUsername());
        assertEquals("12345678913", user.getPersonalId());
        assertEquals("john@email.com", user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
    }

    @Test
    void shouldMapUserEntityToResponseCorrectly() {

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .username("john")
                .personalId("12345678913")
                .email("john@email.com")
                .passwordHash("hashed-password")
                .build();

        UserResponse response = userMapper.mapToResponse(user);

        assertNotNull(response);
        assertEquals(user.getUuid().toString(), response.uuid());
        assertEquals("john", response.username());
        assertEquals("12345678913", response.personalId());
        assertEquals("john@email.com", response.email());
    }

    @Test
    void shouldHandleNullUserGracefullyInResponseMapping() {
        assertThrows(NullPointerException.class,
                () -> userMapper.mapToResponse(null));
    }
}