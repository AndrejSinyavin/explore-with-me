package ru.practicum.ewm.ewmservice.exception;

public class AppEntityNotFoundException extends AppException {
    public AppEntityNotFoundException(String source, String error, String message) {
        super(source, error, message);
    }
}