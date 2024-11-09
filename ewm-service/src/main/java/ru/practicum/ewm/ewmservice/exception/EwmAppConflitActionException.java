package ru.practicum.ewm.ewmservice.exception;

public class EwmAppConflitActionException extends EwmAppException {
    public EwmAppConflitActionException(String source, String error, String message) {
        super(source, error, message);
    }

}
