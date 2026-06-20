package com.assignment.bank.user.controller;

import com.assignment.bank.user.dto.UserRequest;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody @Valid UserRequest userRequest) {
        userService.create(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/personal-id/{personalId}")
    public ResponseEntity<UserResponse> findByPersonalId(
            @PathVariable("personalId") String personalId) {
        return ResponseEntity.ok(
                userService.findByPersonalId(personalId)
        );
    }

}
