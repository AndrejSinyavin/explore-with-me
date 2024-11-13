package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.ewmservice.entity.CategoryEntity;

import java.io.Serializable;

/**
 * DTO для создания категории
 */
public record CategoryNewDto(
        @NotBlank(message = "Не указано название категории события")
        @Size(min = 1, max = 50, message = "Размер сообщения не соответствует заданному диапазону")
        String name
) implements Serializable {
        public CategoryEntity toEntity() {
                var category = new CategoryEntity();
                category.setName(name);
                return category;
        }
}
