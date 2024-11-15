package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.UserEntity;

import java.io.Serializable;

/**
 * DTO для представления данных о пользователе {@link UserEntity}
 */
public record UserDto(
        Long id,
        String email,
        String name
) implements Serializable {
}