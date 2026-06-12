package com.example.base_spring_boot.exceptions;

public class HttpConflictException extends RuntimeException {
    public HttpConflictException(String message) {
        super(message);
    }

    public HttpConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

