package com.assignment.bank.login.service;

import com.assignment.bank.login.dto.LoginRequest;
import com.assignment.bank.login.dto.LoginResponse;
import com.assignment.bank.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        log.info("Authentication attempt initiated for email: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        log.debug("Credentials successfully verified by AuthenticationManager for email: {}", request.email());
        String token = jwtService.generateToken(request.email());

        log.info("Authentication successful. JWT token generated for email: {}", request.email());
        return new LoginResponse(token);
    }
}