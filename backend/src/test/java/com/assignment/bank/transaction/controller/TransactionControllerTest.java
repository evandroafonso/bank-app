package com.assignment.bank.transaction.controller;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.security.JwtService;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.enums.TransactionType;
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
        TransactionRequest request = new TransactionRequest(
                "EE12345678901234",
                new BigDecimal("100.00"),
                Currency.EUR,
                "Deposit test"
        );

        TransactionResponse response = TransactionResponse.builder()
                .uuid("f268a8bc-e3fd-4397-a429-1af09ce1ba88")
                .sourceAmount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("100.00"))
                .exchangeRate(BigDecimal.ONE)
                .currency(Currency.EUR)
                .targetCurrency(Currency.EUR)
                .balance(new BigDecimal("1100.00"))
                .type(TransactionType.CREDIT)
                .description("Deposit test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionService.credit(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("f268a8bc-e3fd-4397-a429-1af09ce1ba88"))
                .andExpect(jsonPath("$.sourceAmount").value(100.00))
                .andExpect(jsonPath("$.balance").value(1100.00))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(transactionService, times(1)).credit(any(TransactionRequest.class));
    }

    @Test
    void shouldDebitAccountSuccessfully() throws Exception {
        TransactionRequest request = new TransactionRequest(
                "EE12345678901234",
                new BigDecimal("50.00"),
                Currency.EUR,
                "Withdrawal test"
        );

        TransactionResponse response = TransactionResponse.builder()
                .uuid("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                .sourceAmount(new BigDecimal("50.00"))
                .convertedAmount(new BigDecimal("50.00"))
                .exchangeRate(BigDecimal.ONE)
                .currency(Currency.EUR)
                .targetCurrency(Currency.EUR)
                .balance(new BigDecimal("950.00"))
                .type(TransactionType.DEBIT)
                .description("Withdrawal test")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionService.debit(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .andExpect(jsonPath("$.sourceAmount").value(50.00))
                .andExpect(jsonPath("$.balance").value(950.00))
                .andExpect(jsonPath("$.type").value("DEBIT"));

        verify(transactionService, times(1)).debit(any(TransactionRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/transactions/credit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}