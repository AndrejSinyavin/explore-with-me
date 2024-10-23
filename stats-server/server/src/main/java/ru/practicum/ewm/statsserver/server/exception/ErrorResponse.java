package ru.practicum.ewm.statsserver.server.exception;

import lombok.Getter;

import java.util.Map;

/**
 * API для передачи сообщений об ошибках клиенту.
 */
@Getter
public class ErrorResponse {
    private final Map<String, String> error;

    public ErrorResponse(String error, String message) {
        this.error = Map.of(error, message);
    }

    public ErrorResponse(Map<String, String> error) {
        this.error = error;
    }

}