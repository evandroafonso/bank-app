package com.assignment.bank.login.controller;

import com.assignment.bank.login.dto.LoginRequest;
import com.assignment.bank.login.dto.LoginResponse;
import com.assignment.bank.login.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }
}
