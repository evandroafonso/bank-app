package com.assignment.bank.exception.handler;

import com.assignment.bank.exception.*;
import com.assignment.bank.exception.model.ErrorCode;
import com.assignment.bank.exception.model.ErrorResponse;
import com.assignment.bank.exception.model.FieldErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
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

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Account not found");
        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND.name());
        assertThat(body.status())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleBusinessException() {

        var exception = new BusinessException("Business rule violated");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Business rule violated");
        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.BUSINESS_ERROR.name());
        assertThat(body.status())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleValidationExceptionWithSingleError() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        FieldError fieldError =
                new FieldError(
                        "request",
                        "amount",
                        "must be greater than zero"
                );

        when(exception.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<?> response =
                handler.handleValidation(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Object body = response.getBody();

        assertThat(body).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<FieldErrorResponse> errors =
                (List<FieldErrorResponse>) body;

        assertThat(errors).hasSize(1);

        assertThat(errors.get(0).field())
                .isEqualTo("amount");

        assertThat(errors.get(0).message())
                .isEqualTo("must be greater than zero");
    }

    @Test
    void shouldHandleValidationExceptionWithMultipleErrors() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        FieldError error1 =
                new FieldError(
                        "request",
                        "amount",
                        "invalid amount"
                );

        FieldError error2 =
                new FieldError(
                        "request",
                        "currency",
                        "currency is required"
                );

        when(exception.getFieldErrors())
                .thenReturn(List.of(error1, error2));

        ResponseEntity<?> response =
                handler.handleValidation(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Object body = response.getBody();

        assertThat(body).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<FieldErrorResponse> errors =
                (List<FieldErrorResponse>) body;

        assertThat(errors).hasSize(2);

        assertThat(errors.get(0).field())
                .isEqualTo("amount");

        assertThat(errors.get(0).message())
                .isEqualTo("invalid amount");

        assertThat(errors.get(1).field())
                .isEqualTo("currency");

        assertThat(errors.get(1).message())
                .isEqualTo("currency is required");
    }

    @Test
    void shouldHandleValidationExceptionWithEmptyErrors() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getFieldErrors())
                .thenReturn(List.of());

        ResponseEntity<?> response =
                handler.handleValidation(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Object body = response.getBody();

        assertThat(body).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<FieldErrorResponse> errors =
                (List<FieldErrorResponse>) body;

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldHandleGenericException() {

        Exception exception = new RuntimeException("unexpected");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.message())
                .isEqualTo("Unexpected error");
        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.INTERNAL_ERROR.name());
        assertThat(body.status())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleAccessDeniedException() {

        var exception = new AccessDeniedException("forbidden");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo(
                        "Access denied - you don't have permission to access this resource"
                );

        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.FORBIDDEN.name());

        assertThat(body.status())
                .isEqualTo(HttpStatus.FORBIDDEN.value());

        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<Object> response =
                handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isEqualTo("Invalid or missing request body");
    }

    @Test
    void shouldHandleInsufficientBalanceException() {

        var exception =
                new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientBalance(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo("Insufficient balance");

        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE.name());

        assertThat(body.status())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleCurrencyMismatchException() {

        var exception =
                new CurrencyMismatchException("Currencies do not match");

        ResponseEntity<ErrorResponse> response =
                handler.handleCurrencyMismatch(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo("Currencies do not match");

        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.CURRENCY_MISMATCH.name());

        assertThat(body.status())
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());

        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleFraudDetectedException() {

        var exception =
                new FraudDetectedException("fraud detected");

        ResponseEntity<ErrorResponse> response =
                handler.handleFraudDetected(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo(
                        "Transaction could not be processed. Please contact support."
                );

        assertThat(body.errorCode())
                .isEqualTo(ErrorCode.TRANSACTION_DENIED.name());

        assertThat(body.status())
                .isEqualTo(HttpStatus.FORBIDDEN.value());

        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void shouldPopulateTimestampForAllHandlers() {

        LocalDateTime before = LocalDateTime.now();

        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(
                        new BusinessException("error")
                );

        LocalDateTime after = LocalDateTime.now();

        ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.timestamp())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }
}