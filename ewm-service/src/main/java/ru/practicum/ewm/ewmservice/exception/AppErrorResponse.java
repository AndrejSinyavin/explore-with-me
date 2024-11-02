package ru.practicum.ewm.ewmservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * API для передачи сообщений об ошибках клиенту.
 */
public record AppErrorResponse(
        String status,
        String reason,
        String message,
        String timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> errors) {
        public AppErrorResponse(String status, String reason, String message, String timestamp) {
                this(status, reason, message, timestamp, null);
        }
}