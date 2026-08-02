package com.whatsflow.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
