package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.CompilationEntity}
 */
public record CompilationUpdateRequestDto(
        Set<Long> events,
        Boolean pinned,
        @Size(min = 1, max = 50, message = "Размер сообщения не соответствует заданному диапазону")
        String title
) implements Serializable {
}