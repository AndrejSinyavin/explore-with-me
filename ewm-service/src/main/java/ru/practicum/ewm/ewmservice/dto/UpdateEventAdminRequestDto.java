package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

public record UpdateEventAdminRequestDto(
        String annotation,
        Integer category,
        String description,
        String eventDate,
        LocationDto location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        String stateAction,
        String title
) implements Serializable {
}
