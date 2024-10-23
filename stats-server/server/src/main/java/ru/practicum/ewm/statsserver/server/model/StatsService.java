package ru.practicum.ewm.statsserver.server.model;

import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;

import java.util.List;

/**
 * Интерфейс сервиса статистики
 */
public interface StatsService {

    void add(EndpointHitEntity endpointHitEntity);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
