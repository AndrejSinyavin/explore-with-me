package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;

/**
 * DTO для запроса от потенциального участника на участие в событии, рассматривается автором афиши события
 */
public record ParticipationRequestDto(
        Long id,
        Long event,
        String status,
        Long requester,
        String created
) implements Serializable {
}