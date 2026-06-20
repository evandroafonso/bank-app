package com.assignment.bank.login.controller;

import com.assignment.bank.login.dto.LoginRequest;
import com.assignment.bank.login.dto.LoginResponse;
import com.assignment.bank.login.service.AuthenticationService;
import com.assignment.bank.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthenticationService service;
    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@email.com",
                "password123"
        );

        when(service.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        LoginRequest invalidRequest = new LoginRequest(
                "",
                ""
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@email.com",
                "password123"
        );

        when(service.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldCallCorrectEndpoint() throws Exception {

        when(service.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "email": "user@email.com",
                                      "password": "123456"
                                    }
                                """))
                .andExpect(status().isOk());
    }
}