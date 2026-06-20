package com.assignment.bank.login.service;

import com.assignment.bank.login.dto.LoginRequest;
import com.assignment.bank.login.dto.LoginResponse;
import com.assignment.bank.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "john@email.com",
                "password123"
        );

        String expectedToken = "jwt-token";

        when(jwtService.generateToken(request.email()))
                .thenReturn(expectedToken);

        // Act
        LoginResponse response = authenticationService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.token());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService, times(1))
                .generateToken(request.email());
    }

    @Test
    void shouldCallAuthenticationManagerWithCorrectCredentials() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "user@email.com",
                "123456"
        );

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        // Act
        authenticationService.login(request);

        // Assert
        verify(authenticationManager)
                .authenticate(argThat(auth ->
                        auth.getName().equals("user@email.com") &&
                                auth.getCredentials().equals("123456")
                ));
    }

    @Test
    void shouldCallJwtServiceWithCorrectEmail() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "test@email.com",
                "pass"
        );

        when(jwtService.generateToken("test@email.com"))
                .thenReturn("token");

        // Act
        authenticationService.login(request);

        // Assert
        verify(jwtService).generateToken("test@email.com");
    }
}