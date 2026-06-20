package com.assignment.bank.user.service;

import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.mapper.UserMapper;
import com.assignment.bank.user.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void create(UserRequest userRequest) {
        String passwordHash = passwordEncoder.encode(userRequest.password());
        var userEntity = userMapper.mapToEntity(userRequest, passwordHash);
        userRepository.save(userEntity);
    }

    public UserResponse findByPersonalId(String personalId) {
        User user = userRepository.findByPersonalId(personalId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.mapToResponse(user);
    }

}
