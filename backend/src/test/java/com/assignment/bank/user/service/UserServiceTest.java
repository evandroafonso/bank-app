package com.assignment.bank.user.service;

import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.mapper.UserMapper;
import com.assignment.bank.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        UserRequest request = new UserRequest(
                "john",
                "12345678913",
                "john@email.com",
                "123456"
        );

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashed-password");

        User userEntity = mock(User.class);

        when(userMapper.mapToEntity(request, "hashed-password"))
                .thenReturn(userEntity);

        userService.create(request);

        verify(passwordEncoder).encode("123456");
        verify(userMapper).mapToEntity(request, "hashed-password");
        verify(userRepository).save(userEntity);
    }

    @Test
    void shouldThrowExceptionWhenPasswordEncoderFails() {
        UserRequest request = new UserRequest(
                "john",
                "12345678913",
                "john@email.com",
                "123456"
        );

        when(passwordEncoder.encode(anyString()))
                .thenThrow(new RuntimeException("Encoder error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.create(request)
        );

        assertEquals("Encoder error", ex.getMessage());

        verify(passwordEncoder).encode("123456");
        verifyNoInteractions(userMapper);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenMapperFails() {
        UserRequest request = new UserRequest(
                "john",
                "12345678913",
                "john@email.com",
                "123456"
        );

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashed-password");

        when(userMapper.mapToEntity(any(), anyString()))
                .thenThrow(new RuntimeException("Mapper error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.create(request)
        );

        assertEquals("Mapper error", ex.getMessage());

        verify(passwordEncoder).encode("123456");
        verify(userMapper).mapToEntity(request, "hashed-password");
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {
        UserRequest request = new UserRequest(
                "john",
                "12345678913",
                "john@email.com",
                "123456"
        );

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");

        User userEntity = mock(User.class);

        when(userMapper.mapToEntity(any(), anyString()))
                .thenReturn(userEntity);

        when(userRepository.save(any()))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.create(request)
        );

        assertEquals("DB error", ex.getMessage());

        verify(userRepository).save(userEntity);
    }

    @Test
    void shouldReturnUserResponseWhenPersonalIdExists() {
        String personalId = "12345678913";

        User user = mock(User.class);
        UserResponse response = mock(UserResponse.class);

        when(userRepository.findByPersonalId(personalId))
                .thenReturn(java.util.Optional.of(user));

        when(userMapper.mapToResponse(user))
                .thenReturn(response);

        UserResponse result = userService.findByPersonalId(personalId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(userRepository).findByPersonalId(personalId);
        verify(userMapper).mapToResponse(user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        String personalId = "12345678913";

        when(userRepository.findByPersonalId(personalId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                com.assignment.bank.exception.NotFoundException.class,
                () -> userService.findByPersonalId(personalId)
        );

        verify(userRepository).findByPersonalId(personalId);
        verifyNoInteractions(userMapper);
    }
}