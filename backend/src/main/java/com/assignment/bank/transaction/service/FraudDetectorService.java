package com.assignment.bank.transaction.service;

import com.assignment.bank.exception.FraudDetectedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class FraudDetectorService {

    private static final BigDecimal FRAUD_TRIGGER_AMOUNT = new BigDecimal("5");
    private static final String SUCCESS_URL = "https://httpstatuses.maor.io/200";
    private static final String FRAUD_URL = "https://httpstatuses.maor.io/403";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FraudDetectorService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public void check(BigDecimal amount, String iban) {
        String url = isFraudAmount(amount) ? FRAUD_URL : SUCCESS_URL;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            int code = body.get("code").asInt();

            if (code == 403) {
                throw new FraudDetectedException("Transaction blocked: fraud detected for amount " + amount);
            }

        } catch (FraudDetectedException ex) {
            log.warn("Fraud detected. Transaction was not completed due to suspected fraudulent activity. Account IBAN: {}", iban);
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Fraud check service unavailable", ex);
        }
    }

    private boolean isFraudAmount(BigDecimal amount) {
        return amount.compareTo(FRAUD_TRIGGER_AMOUNT) == 0;
    }
}