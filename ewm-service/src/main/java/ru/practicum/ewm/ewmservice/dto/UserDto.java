package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.EntityUser;

import java.io.Serializable;

/**
 * DTO for {@link EntityUser}
 */
public record UserDto(
        Long id,
        String email,
        String name
) implements Serializable {
}