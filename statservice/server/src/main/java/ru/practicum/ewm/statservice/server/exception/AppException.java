package ru.practicum.ewm.statservice.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Шаблон для создания кастомных исключений сервиса
 */
@AllArgsConstructor
@Getter
public abstract class AppException extends RuntimeException {
    private final String source;
    private final String error;
    private final String message;

}