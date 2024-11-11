package ru.practicum.ewm.statsserver.server.exception;

public class AppBadRequestException extends AppException {

    public AppBadRequestException(String source, String error, String message) {
        super(source, error, message);
    }

}