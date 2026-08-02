package com.whatsflow.exception;

import com.whatsflow.common.util.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex.getErrorCode().getCode(), ex.getMessage(),
                ex.getHttpStatus(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return buildResponse(ErrorCode.VALIDATION_ERROR.getCode(), message,
                HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.INVALID_CREDENTIALS.getCode(), ErrorCode.INVALID_CREDENTIALS.getMessage(),
                HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage(),
                HttpStatus.FORBIDDEN, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return buildResponse(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(String code, String message,
                                                        HttpStatus status, String path) {
        ErrorResponse body = ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .correlationId(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    @Value
    @Builder
    public static class ErrorResponse {
        String code;
        String message;
        String path;
        Instant timestamp;
        String correlationId;
    }
}
