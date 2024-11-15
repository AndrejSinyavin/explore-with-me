package ru.practicum.ewm.ewmservice.dto;

import ru.practicum.ewm.ewmservice.entity.EventRatesEntity;

import java.io.Serializable;

/**
 * DTO для представления подробной информации о рейтингах и доступных полях события {@link EventRatesEntity}
 */
public record EventShortRateDto(
        Long eventId,
        Long expectationRate,
        String satisfactionRate,
        Long eventViews,
        String eventTitle,
        String eventCategory,
        String initiator,
        String eventDateTime,
        String eventPublishedOn
) implements Serializable {
}