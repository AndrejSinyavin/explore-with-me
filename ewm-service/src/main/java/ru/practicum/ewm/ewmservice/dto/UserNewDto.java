package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.ewmservice.entity.UserEntity;

import java.io.Serializable;

/**
 * DTO {@link UserEntity} для создания анкеты пользователя сервиса.
 */
public record UserNewDto(
        @Email(message = "Email имеет неверный формат")
        @NotBlank(message = "Не указан Email")
        @Size(min = 6, max = 254, message = "Размер сообщения не соответствует заданному диапазону")
        String email,

        @NotBlank(message = "Не указаны ФИО пользователя")
        @Size(min = 2, max = 250, message = "Размер сообщения не соответствует заданному диапазону")
        String name
) implements Serializable {
        public UserEntity toEntity() {
                var entity = new UserEntity();
                entity.setEmail(email);
                entity.setName(name);
                return entity;
        }
}