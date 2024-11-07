package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.ParticipationRequestEntity}
 */
public record ParticipationRequestDto(
        Long id,
        Long event,
        String status,
        Long requester,
        String created
) implements Serializable {
}