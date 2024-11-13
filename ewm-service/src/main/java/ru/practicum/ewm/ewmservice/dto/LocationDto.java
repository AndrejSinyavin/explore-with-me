package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.ewmservice.entity.EventLocationEntity;

import java.io.Serializable;

/**
 * DTO для представления данных геолокации {@link EventLocationEntity}
 */
public record LocationDto(
        @NotNull(message = "Не указаны координаты широты") Float lat,
        @NotNull(message = "Не указаны координаты долготы") Float lon
) implements Serializable {
}