package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.EntityEvent}
 */
public record EventShortDto(
        Long id,
        String annotation,
        CategoryDto entityCategory,
        Long confirmedRequests,
        String eventDate,
        UserShortDto initiator,
        Boolean paid,
        String title,
        Long views
) implements Serializable {
}