package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO для удобного просмотра рейтингов и доступных полей {@link ru.practicum.ewm.ewmservice.entity.EventEntity}
 */
public record EventRateDto(
        Long id,
        String annotation,
        Long expectationRate,
        String satisfactionRate,
        String category,
        String initiator,
        Long initiatorRate,
        Long views,
        Long confirmedRequests,
        String eventDate,
        String createdOn,
        Boolean paid,
        LocationDto location,
        String description
) implements Serializable {
}