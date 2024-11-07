package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.io.Serializable;

/**
 * DTO for {@link EventEntity}
 */
public record EventFullDto(
        Long id,
        String annotation,
        CategoryDto category,
        Long confirmedRequests,
        String createdOn,
        String description,
        String eventDate,
        UserShortDto initiator,
        LocationDto location,
        Boolean paid,
        Integer participantLimit,
        String publishedOn,
        Boolean requestModeration,
        String title,
        Long views,
        String state
) implements Serializable {
}