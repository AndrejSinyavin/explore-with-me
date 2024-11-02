package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.EntityEventLocation}
 */
public record LocationDto(
        @NotNull(message = "Не указаны координаты широты") Float lat,
        @NotNull(message = "Не указаны координаты долготы") Float lon
) implements Serializable {
}