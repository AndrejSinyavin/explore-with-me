package ru.practicum.ewm.ewmservice.exception;

public class EwmAppConflictActionException extends EwmAppException {
    public EwmAppConflictActionException(String source, String error, String message) {
        super(source, error, message);
    }

}
