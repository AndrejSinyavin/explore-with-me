package ru.practicum.ewm.ewmservice.exception;

public class EwmAppEntityNotFoundException extends EwmAppException {
    public EwmAppEntityNotFoundException(String source, String error, String message) {
        super(source, error, message);
    }
}