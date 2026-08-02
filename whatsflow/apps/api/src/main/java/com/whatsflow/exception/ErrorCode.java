package com.whatsflow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "Access denied", HttpStatus.FORBIDDEN),
    CONFLICT("CONFLICT", "Resource conflict", HttpStatus.CONFLICT),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already registered", HttpStatus.CONFLICT),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TENANT_REQUIRED("TENANT_REQUIRED", "Tenant context is required", HttpStatus.BAD_REQUEST),
    TENANT_MISMATCH("TENANT_MISMATCH", "Tenant mismatch", HttpStatus.FORBIDDEN),
    BUSINESS_RULE("BUSINESS_RULE", "Business rule violation", HttpStatus.UNPROCESSABLE_ENTITY);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
