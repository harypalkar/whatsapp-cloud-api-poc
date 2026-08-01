package com.example.whatsapp.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final int statusCode;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST.value());
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status.value());
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatus getHttpStatus() {
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status != null ? status : HttpStatus.BAD_REQUEST;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
