package ru.practicum.ewm.ewmservice.exception;

public class EwmAppRequestValidateException extends EwmAppException {
    public EwmAppRequestValidateException(String source, String error, String message) {
        super(source, error, message);
    }
}
