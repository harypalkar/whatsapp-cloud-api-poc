package com.example.whatsapp.exception;

public class WhatsAppApiException extends RuntimeException {

    private final int statusCode;

    public WhatsAppApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
