package com.assignment.bank.account.controller;

import com.assignment.bank.account.dto.AccountBalanceResponse;
import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.service.AccountService;
import com.assignment.bank.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateAccountAndReturn201() throws Exception {
        AccountRequest request = new AccountRequest(Currency.EUR);
        AccountResponse response = AccountResponse.builder()
                .uuid("123e4567-e89b-12d3-a456-426614174000")
                .iban("EE142212345678901234")
                .currency(String.valueOf(Currency.EUR))
                .balance(new BigDecimal("2.3212"))
                .user(null)
                .build();

        when(accountService.save(any(AccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.iban").value("EE142212345678901234"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value("2.3212"));

        verify(accountService, Mockito.times(1)).save(any(AccountRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindAllAccountsSuccessfully() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .iban("EE12345678901234")
                .currency("EUR")
                .balance(BigDecimal.ZERO)
                .build();

        when(accountService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("EE12345678901234"));

        verify(accountService, times(1)).findAll();
    }

    @Test
    void shouldFindByIbanSuccessfully() throws Exception {
        String iban = "EE12345678901234";
        AccountResponse response = AccountResponse.builder()
                .iban(iban)
                .currency("EUR")
                .balance(BigDecimal.TEN)
                .build();

        when(accountService.findByIban(iban)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(accountService, times(1)).findByIban(iban);
    }

    @Test
    void shouldReturnNotFoundWhenIbanDoesNotExist() throws Exception {
        String iban = "NON-EXISTENT";

        when(accountService.findByIban(iban))
                .thenThrow(new com.assignment.bank.exception.NotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(accountService, times(1)).findByIban(iban);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() throws Exception {
        when(accountService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(accountService, times(1)).findAll();
    }

    @Test
    void shouldGetBalanceSuccessfully() throws Exception {
        String iban = "EE12345678901234";
        AccountBalanceResponse response = new AccountBalanceResponse(iban, "EUR", new BigDecimal("1500.50"));

        when(accountService.getBalance(iban)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/balance/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(1500.50));

        verify(accountService, times(1)).getBalance(iban);
    }

    @Test
    void shouldReturnNotFoundWhenGetBalanceForNonExistentAccount() throws Exception {
        String iban = "INVALID-IBAN";

        when(accountService.getBalance(iban))
                .thenThrow(new com.assignment.bank.exception.NotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/balance/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getBalance(iban);
    }

}