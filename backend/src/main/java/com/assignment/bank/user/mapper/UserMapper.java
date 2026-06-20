package com.assignment.bank.user.mapper;

import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User mapToEntity(UserRequest userRequest, String passwordHash) {
        return User.builder()
                .username(userRequest.username())
                .personalId(userRequest.personalId())
                .email(userRequest.email())
                .passwordHash(passwordHash)
                .build();
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .uuid(String.valueOf(user.getUuid()))
                .username(user.getUsername())
                .personalId(user.getPersonalId())
                .email(user.getEmail())
                .build();
    }

}
