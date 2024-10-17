package ru.practicum.model.server;

import ru.practicum.model.dto.ViewStatsDto;

import java.util.List;

/**
 * Интерфейс сервиса статистики
 */
public interface StatsService {

    void add(EndpointHitEntity endpointHitEntity);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
