package ru.practicum.ewm.ewmservice.exception;

public class AppInternalServiceException extends AppException {
    public AppInternalServiceException(String source, String error, String message) {
        super(source, error, message);
    }
}