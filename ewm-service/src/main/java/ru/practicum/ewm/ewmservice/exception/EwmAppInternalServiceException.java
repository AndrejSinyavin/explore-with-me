package ru.practicum.ewm.ewmservice.exception;

public class EwmAppInternalServiceException extends EwmAppException {
    public EwmAppInternalServiceException(String source, String error, String message) {
        super(source, error, message);
    }
}