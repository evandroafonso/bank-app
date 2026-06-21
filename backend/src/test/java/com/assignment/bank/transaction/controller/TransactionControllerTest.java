package com.assignment.bank.transaction.controller;

import com.assignment.bank.security.JwtService;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreditAccountSuccessfully() throws Exception {
        TransactionRequest request = new TransactionRequest("EE12345678901234", new BigDecimal("100.00"), "Deposit test");
        TransactionResponse response = TransactionResponse.builder()
                .transactionUuid("f268a8bc-e3fd-4397-a429-1af09ce1ba88")
                .amount(new BigDecimal("100.00"))
                .balance(new BigDecimal("1100.00"))
                .description("Deposit test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionService.credit(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionUuid").value("f268a8bc-e3fd-4397-a429-1af09ce1ba88"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.balance").value(1100.00));

        verify(transactionService, times(1)).credit(any(TransactionRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/transactions/credit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}