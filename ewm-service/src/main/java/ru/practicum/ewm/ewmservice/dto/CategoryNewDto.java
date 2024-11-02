package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.ewmservice.entity.EntityCategory;

import java.io.Serializable;

/**
 * DTO
 */
public record CategoryNewDto(
        @NotBlank(message = "Не указано название категории события")
        @Size(min = 1, max = 50, message = "Размер сообщения не соответствует заданному диапазону")
        String name
) implements Serializable {
        public EntityCategory toEntity() {
                var category = new EntityCategory();
                category.setName(name);
                category.setId(0L);
                return category;
        }
}
