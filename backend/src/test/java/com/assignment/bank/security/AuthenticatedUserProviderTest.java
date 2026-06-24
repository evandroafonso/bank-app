package com.assignment.bank.security;

import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserProviderTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnUserWhenAuthenticated() {
        String email = "test@bank.com";
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                java.util.Collections.emptyList()
        );

        when(userDetails.getUsername()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);

        SecurityContextHolder.getContext().setAuthentication(auth);

        User result = authenticatedUserProvider.get();

        assertNotNull(result);
        assertEquals(user, result);
        verify(userService).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenNotAuthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> authenticatedUserProvider.get());
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsAnonymous() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> authenticatedUserProvider.get());
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNotUserDetails() {
        Authentication auth = new UsernamePasswordAuthenticationToken("invalidPrincipal", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> authenticatedUserProvider.get());
    }
}