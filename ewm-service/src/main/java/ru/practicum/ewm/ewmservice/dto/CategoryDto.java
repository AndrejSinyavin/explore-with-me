package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

public record CategoryDto(
        Long id,
        String name
) implements Serializable {
}
