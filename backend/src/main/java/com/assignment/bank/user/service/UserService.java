package com.assignment.bank.user.service;

import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.mapper.UserMapper;
import com.assignment.bank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public void create(UserRequest userRequest) {
        log.info("Initiating user creation for email: {}", userRequest.email());

        String passwordHash = passwordEncoder.encode(userRequest.password());
        var userEntity = userMapper.mapToEntity(userRequest, passwordHash);
        userRepository.save(userEntity);

        log.info("User successfully created with email: {}", userRequest.email());
    }

    public UserResponse findByPersonalId(String personalId) {
        log.debug("Searching for user with personal ID: {}", personalId);

        User user = userRepository.findByPersonalId(personalId)
                .orElseThrow(() -> {
                    log.warn("Retrieval failed: User not found with personal ID: {}", personalId);
                    return new NotFoundException("User not found");
                });

        return userMapper.mapToResponse(user);
    }

    public User findByEmail(String email) {
        log.debug("Searching for user with email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Retrieval failed: User not found with email: {}", email);
                    return new NotFoundException("User not found");
                });
    }
}