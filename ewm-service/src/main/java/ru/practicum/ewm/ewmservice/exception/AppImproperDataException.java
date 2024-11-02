package ru.practicum.ewm.ewmservice.exception;

public class AppImproperDataException extends AppException {
    public AppImproperDataException(String source, String error, String message) {
        super(source, error, message);
    }

}
