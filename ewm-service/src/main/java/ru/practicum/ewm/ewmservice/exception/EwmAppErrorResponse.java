package ru.practicum.ewm.ewmservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * API для передачи сообщений об ошибках клиенту.
 */
public record EwmAppErrorResponse(
        String status,
        String reason,
        String message,
        String timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> errors) {
        public EwmAppErrorResponse(String status, String reason, String message, String timestamp) {
                this(status, reason, message, timestamp, null);
        }
}