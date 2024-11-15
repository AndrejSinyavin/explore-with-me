package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.io.Serializable;

/**
 * DTO для события, минимальное представление для использования в отчетах {@link EventEntity}
 */
public record EventShortDto(
        Long id,
        String annotation,
        CategoryDto category,
        Long confirmedRequests,
        String eventDate,
        UserShortDto initiator,
        Boolean paid,
        String title,
        Long views
) implements Serializable {
}