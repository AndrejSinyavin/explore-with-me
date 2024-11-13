package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO для упрощенного представления данных о пользователе в других представлениях
 */
public record UserShortDto(
        Long id,
        String name
) implements Serializable {
}