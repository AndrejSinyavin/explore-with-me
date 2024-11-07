package ru.practicum.ewm.statsserver.server.model;

import ru.practicum.ewm.statsserver.server.exception.AppException;

public class StatsAppAcceptedException extends AppException {
    public StatsAppAcceptedException(String source, String error, String message) {
        super(source, error, message);
    }
}
