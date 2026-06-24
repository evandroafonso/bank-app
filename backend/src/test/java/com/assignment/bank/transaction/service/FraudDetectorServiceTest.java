package com.assignment.bank.transaction.service;

import com.assignment.bank.exception.FraudDetectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectorServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FraudDetectorService fraudDetectorService;

    @Test
    void shouldPassWhenAmountIsNotFraudTrigger() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("{\"code\":200,\"description\":\"OK\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        var jsonNode = new ObjectMapper().readTree("{\"code\":200,\"description\":\"OK\"}");
        when(objectMapper.readTree("{\"code\":200,\"description\":\"OK\"}")).thenReturn(jsonNode);

        assertDoesNotThrow(() -> fraudDetectorService.check(new BigDecimal("100.00"), "EE382200221020145685"));

        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void shouldThrowFraudDetectedExceptionWhenAmountIsFraudTrigger() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("{\"code\":403,\"description\":\"Forbidden\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        var jsonNode = new ObjectMapper().readTree("{\"code\":403,\"description\":\"Forbidden\"}");
        when(objectMapper.readTree("{\"code\":403,\"description\":\"Forbidden\"}")).thenReturn(jsonNode);

        FraudDetectedException ex = assertThrows(FraudDetectedException.class,
                () -> fraudDetectorService.check(new BigDecimal("5"), "EE382200221020145685"));

        assertEquals("Transaction blocked: fraud detected for amount 5", ex.getMessage());
    }

    @Test
    void shouldThrowFraudDetectedExceptionWhenExternalServiceReturns403ForNonTriggerAmount() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("{\"code\":403,\"description\":\"Forbidden\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        var jsonNode = new ObjectMapper().readTree("{\"code\":403,\"description\":\"Forbidden\"}");
        when(objectMapper.readTree("{\"code\":403,\"description\":\"Forbidden\"}")).thenReturn(jsonNode);

        assertThrows(FraudDetectedException.class,
                () -> fraudDetectorService.check(new BigDecimal("100.00"), "EE382200221020145685"));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenHttpClientFails() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fraudDetectorService.check(new BigDecimal("100.00"), "EE382200221020145685"));

        assertEquals("Fraud check service unavailable", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenResponseBodyIsInvalid() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("invalid json");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(objectMapper.readTree("invalid json")).thenThrow(new RuntimeException("Invalid JSON"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> fraudDetectorService.check(new BigDecimal("100.00"), "EE382200221020145685"));

        assertEquals("Fraud check service unavailable", ex.getMessage());
    }

    @Test
    void shouldCallFraudUrlWhenAmountEqualsFive() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("{\"code\":403,\"description\":\"Forbidden\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        var jsonNode = new ObjectMapper().readTree("{\"code\":403,\"description\":\"Forbidden\"}");
        when(objectMapper.readTree("{\"code\":403,\"description\":\"Forbidden\"}")).thenReturn(jsonNode);

        assertThrows(FraudDetectedException.class,
                () -> fraudDetectorService.check(new BigDecimal("5"), "EE382200221020145685"));

        verify(httpClient).send(
                argThat(req -> req.uri().toString().equals("https://httpstatuses.maor.io/403")),
                any(HttpResponse.BodyHandler.class)
        );
    }

    @Test
    void shouldCallSuccessUrlWhenAmountDoesNotEqualFive() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("{\"code\":200,\"description\":\"OK\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        var jsonNode = new ObjectMapper().readTree("{\"code\":200,\"description\":\"OK\"}");
        when(objectMapper.readTree("{\"code\":200,\"description\":\"OK\"}")).thenReturn(jsonNode);

        assertDoesNotThrow(() -> fraudDetectorService.check(new BigDecimal("99.00"), "EE382200221020145685"));

        verify(httpClient).send(
                argThat(req -> req.uri().toString().equals("https://httpstatuses.maor.io/200")),
                any(HttpResponse.BodyHandler.class)
        );
    }
}