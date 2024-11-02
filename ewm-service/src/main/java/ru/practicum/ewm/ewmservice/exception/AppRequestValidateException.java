package ru.practicum.ewm.ewmservice.exception;

public class AppRequestValidateException extends AppException {
    public AppRequestValidateException(String source, String error, String message) {
        super(source, error, message);
    }
}
