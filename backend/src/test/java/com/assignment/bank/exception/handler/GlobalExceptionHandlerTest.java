package com.assignment.bank.exception.handler;

import com.assignment.bank.exception.*;
import com.assignment.bank.exception.model.ErrorCode;
import com.assignment.bank.exception.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleNotFoundException() {
        var exception = new NotFoundException("Account not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Account not found");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.NOT_FOUND.name());
        assertThat(body.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleBusinessException() {
        var exception = new BusinessException("Business rule violated");

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Business rule violated");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.BUSINESS_ERROR.name());
        assertThat(body.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleValidationExceptionWithSingleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("request", "amount", "must be greater than zero");
        when(exception.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("must be greater than zero");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(body.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleValidationExceptionWithMultipleErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError error1 = new FieldError("request", "amount", "invalid amount");
        FieldError error2 = new FieldError("request", "currency", "currency is required");
        when(exception.getFieldErrors()).thenReturn(List.of(error1, error2));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("invalid amount; currency is required");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(body.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleValidationExceptionWithEmptyErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEmpty();
        assertThat(body.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(body.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new RuntimeException("unexpected");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Unexpected error");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.name());
        assertThat(body.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleAccessDeniedException() {
        var exception = new AccessDeniedException("forbidden");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Access denied - you don't have permission to access this resource");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.FORBIDDEN.name());
        assertThat(body.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleInsufficientBalanceException() {
        var exception = new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Insufficient balance");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE.name());
        assertThat(body.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleCurrencyMismatchException() {
        var exception = new CurrencyMismatchException("Currencies do not match");

        ResponseEntity<ErrorResponse> response = handler.handleCurrencyMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Currencies do not match");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.CURRENCY_MISMATCH.name());
        assertThat(body.status()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleFraudDetectedException() {
        var exception = new FraudDetectedException("fraud detected");

        ResponseEntity<ErrorResponse> response = handler.handleFraudDetected(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Transaction could not be processed. Please contact support.");
        assertThat(body.errorCode()).isEqualTo(ErrorCode.TRANSACTION_DENIED.name());
        assertThat(body.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldPopulateTimestampForAllHandlers() {
        LocalDateTime before = LocalDateTime.now();

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(new BusinessException("error"));

        LocalDateTime after = LocalDateTime.now();
        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.timestamp()).isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void shouldHandleHttpMessageNotReadableExceptionWithNullCause() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getCause()).thenReturn(null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Malformed JSON request");
    }

    @Test
    void shouldHandleHttpMessageNotReadableExceptionWithInvalidEnumAndPath() {
        JacksonException.Reference reference = new JacksonException.Reference(null, "status");

        InvalidFormatException cause = new InvalidFormatException(
                null,
                "Error",
                "INVALID_VALUE",
                TestEnum.class
        );
        cause.prependPath(reference);

        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getCause()).thenReturn(cause);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        String expectedMessage = String.format(
                "Invalid value 'INVALID_VALUE' for field 'status'. Accepted values: %s",
                Arrays.toString(TestEnum.values())
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldHandleHttpMessageNotReadableExceptionWithInvalidEnumAndEmptyPath() {
        InvalidFormatException cause = new InvalidFormatException(
                null,
                "Error",
                "INVALID_VALUE",
                TestEnum.class
        );

        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getCause()).thenReturn(cause);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        String expectedMessage = String.format(
                "Invalid value 'INVALID_VALUE' for field 'field'. Accepted values: %s",
                Arrays.toString(TestEnum.values())
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }

    private enum TestEnum {
        VALUE_1, VALUE_2
    }

}