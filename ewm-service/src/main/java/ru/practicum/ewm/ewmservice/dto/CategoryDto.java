package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO для категории
 */
public record CategoryDto(
        Long id,
        String name
) implements Serializable {
}
