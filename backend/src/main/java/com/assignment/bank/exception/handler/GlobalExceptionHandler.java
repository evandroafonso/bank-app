package com.assignment.bank.exception.handler;

import com.assignment.bank.exception.*;
import com.assignment.bank.exception.model.ErrorCode;
import com.assignment.bank.exception.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildError(
            String message,
            ErrorCode errorCode,
            HttpStatus status) {
        return new ErrorResponse(
                message,
                errorCode.name(),
                status.value(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.BUSINESS_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(buildError(
                        message,
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(buildError(
                        "Unexpected error",
                        ErrorCode.INTERNAL_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(
                        "Access denied - you don't have permission to access this resource",
                        ErrorCode.FORBIDDEN,
                        HttpStatus.FORBIDDEN
                ));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex) {
        return ResponseEntity.badRequest()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.INSUFFICIENT_BALANCE,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(
            CurrencyMismatchException ex) {
        return ResponseEntity.unprocessableContent()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.CURRENCY_MISMATCH,
                        HttpStatus.UNPROCESSABLE_CONTENT
                ));
    }

    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<ErrorResponse> handleFraudDetected(
            FraudDetectedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(
                        "Transaction could not be processed. Please contact support.",
                        ErrorCode.TRANSACTION_DENIED,
                        HttpStatus.FORBIDDEN
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty()
                    ? "field"
                    : ife.getPath().getLast().getPropertyName();

            if (ife.getTargetType().isEnum()) {
                Object[] validValues = ife.getTargetType().getEnumConstants();
                message = String.format(
                        "Invalid value '%s' for field '%s'. Accepted values: %s",
                        ife.getValue(),
                        fieldName,
                        Arrays.toString(validValues)
                );
            } else if (Number.class.isAssignableFrom(ife.getTargetType())
                    || ife.getTargetType().isPrimitive()) {
                message = String.format(
                        "Invalid numeric value for field '%s'. The value is out of range or improperly formatted.",
                        fieldName
                );
            } else {
                message = String.format(
                        "Invalid value for field '%s'.",
                        fieldName
                );
            }
        }
        return ResponseEntity.badRequest()
                .body(buildError(
                        message,
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

}
