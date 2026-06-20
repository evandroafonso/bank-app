package com.assignment.bank.user.controller;

import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldCreateUserAndReturnCreated() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("john")
                .personalId("12345678913")
                .email("john@email.com")
                .password("password123")
                .build();

        doNothing().when(userService).create(request);

        // Act
        ResponseEntity<Void> response = userController.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService, times(1)).create(request);
    }

    @Test
    void shouldFindUserByPersonalIdAndReturnUserResponse() {
        // Arrange
        String personalId = "12345678913";

        UserResponse expectedResponse = UserResponse.builder()
                .username("john")
                .personalId(personalId)
                .email("john@email.com")
                .build();

        when(userService.findByPersonalId(personalId))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<UserResponse> response =
                userController.findByPersonalId(personalId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());

        verify(userService, times(1)).findByPersonalId(personalId);
    }
}