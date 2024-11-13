package ru.practicum.ewm.ewmservice.service;

import ru.practicum.ewm.ewmservice.dto.EventRateDto;
import ru.practicum.ewm.ewmservice.dto.EventsStatsDto;

import java.util.List;

public interface RatingApiService {
    void addEventExpectationRating(Long uId, Long eId);

    EventRateDto getEventRating(Long eId);

    List<EventsStatsDto> getRatings(Integer top);

    void addEventSatisfactionRating(Long uId, Long eId, String rating);
}
