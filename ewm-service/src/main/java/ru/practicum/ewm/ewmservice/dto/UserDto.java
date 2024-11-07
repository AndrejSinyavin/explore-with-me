package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.UserEntity;

import java.io.Serializable;

/**
 * DTO for {@link UserEntity}
 */
public record UserDto(
        Long id,
        String email,
        String name
) implements Serializable {
}