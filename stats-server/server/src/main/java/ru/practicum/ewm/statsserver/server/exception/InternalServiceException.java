package ru.practicum.ewm.statsserver.server.exception;

public class InternalServiceException extends AppException {

    public InternalServiceException(String source, String error, String message) {
        super(source, error, message);
    }

}