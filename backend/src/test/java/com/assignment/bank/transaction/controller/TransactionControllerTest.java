package com.assignment.bank.transaction.controller;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.security.JwtService;
import com.assignment.bank.transaction.dto.BalanceChartPointResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void shouldGetTransactionSuccessfully() throws Exception {
        UUID uuid = UUID.fromString("f268a8bc-e3fd-4397-a429-1af09ce1ba88");

        TransactionResponse response = TransactionResponse.builder()
                .uuid(uuid.toString())
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

        when(transactionService.getTransaction(uuid)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.sourceAmount").value(100.00))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.balance").value(1100.00));

        verify(transactionService, times(1)).getTransaction(uuid);
    }

    @Test
    void shouldGetBalanceChartDataSuccessfully() throws Exception {
        String iban = "EE12345678901234";
        LocalDateTime startDate = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse("2023-12-31T23:59:59");

        BalanceChartPointResponse point1 = new BalanceChartPointResponse(
                LocalDateTime.parse("2023-06-15T10:00:00"),
                new BigDecimal("1000.00")
        );

        BalanceChartPointResponse point2 = new BalanceChartPointResponse(
                LocalDateTime.parse("2023-07-15T10:00:00"),
                new BigDecimal("1500.00")
        );

        List<BalanceChartPointResponse> chartData = List.of(point1, point2);

        when(transactionService.getBalanceChartData(iban, startDate, endDate)).thenReturn(chartData);

        mockMvc.perform(get("/api/transactions/balance-chart")
                        .param("iban", iban)
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].balance").value(1000.00))
                .andExpect(jsonPath("$[1].balance").value(1500.00));

        verify(transactionService, times(1)).getBalanceChartData(iban, startDate, endDate);
    }

    @Test
    void shouldGetTransactionHistorySuccessfully() throws Exception {
        String iban = "EE382200221020145685";
        int page = 0;
        int size = 10;

        TransactionResponse transaction = TransactionResponse.builder()
                .uuid("f268a8bc-e3fd-4397-a429-1af09ce1ba88")
                .type(TransactionType.CREDIT)
                .sourceAmount(new BigDecimal("100.00"))
                .balance(new BigDecimal("1100.00"))
                .build();

        List<TransactionResponse> content = List.of(transaction);
        Page<TransactionResponse> pageResponse = new PageImpl<>(content, PageRequest.of(page, size), 1);

        when(transactionService.getHistory(eq(iban), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/transactions/history/{iban}", iban)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").value("f268a8bc-e3fd-4397-a429-1af09ce1ba88"))
                .andExpect(jsonPath("$.content[0].type").value("CREDIT"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(page));

        verify(transactionService, times(1)).getHistory(eq(iban), any(Pageable.class));
    }

    @Test
    void shouldGetTransactionHistoryWithDefaultPagination() throws Exception {
        String iban = "EE382200221020145685";

        Page<TransactionResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(transactionService.getHistory(eq(iban), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/transactions/history/{iban}", iban)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.pageSize").value(10));

        verify(transactionService).getHistory(eq(iban), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
        ));
    }
}