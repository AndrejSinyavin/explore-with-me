package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.CompilationEntity}
 */
public record CompilationDto(
        Long id,
        Set<EventShortDto> events,
        Boolean pinned,
        String title
) implements Serializable {
}