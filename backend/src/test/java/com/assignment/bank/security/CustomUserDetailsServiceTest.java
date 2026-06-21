package com.assignment.bank.security;

import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void shouldReturnUserDetailsWhenUserExists() {
        // given
        String email = "test@test.com";

        User user = User.builder()
                .email(email)
                .passwordHash("hashed-password")
                .build();

        when(repository.findByEmail(email))
                .thenReturn(Optional.of(user));

        // when
        UserDetails result = service.loadUserByUsername(email);

        // then
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("hashed-password", result.getPassword());

        verify(repository, times(1)).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        String email = "notfound@test.com";

        when(repository.findByEmail(email))
                .thenReturn(Optional.empty());

        // when + then
        UsernameNotFoundException exception =
                assertThrows(UsernameNotFoundException.class,
                        () -> service.loadUserByUsername(email));

        assertEquals("User not found", exception.getMessage());

        verify(repository, times(1)).findByEmail(email);
    }
}