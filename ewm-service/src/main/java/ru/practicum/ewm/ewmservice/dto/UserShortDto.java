package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.EntityUser;

import java.io.Serializable;

/**
 * DTO for {@link EntityUser}
 */
public record UserShortDto(
        Long id,
        String name
) implements Serializable {
}