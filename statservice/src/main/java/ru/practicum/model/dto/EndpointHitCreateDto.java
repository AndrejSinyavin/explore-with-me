package ru.practicum.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.practicum.model.server.EndpointHitEntity;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ru.practicum.StatsServiceApplication.dateTimePattern;

/**
 * DTO для регистрации события 'Обработан запрос клиента на эндпоинт API приложения'
 *
 * @param app компонент, обработавший запрос
 * @param uri на какой эндпоинт был запрос
 * @param ip с какого IP был выполнен запрос
 * @param timestamp дата и время выполнения запроса
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EndpointHitCreateDto(
        String app,
        String uri,
        String ip,
        String timestamp) implements Serializable {
    public EndpointHitEntity toEntity() {
        return new EndpointHitEntity(0L, app, uri, ip, Instant.from(LocalDateTime
                .parse(timestamp, DateTimeFormatter.ofPattern(dateTimePattern))
                .atZone(ZoneId.of("GMT0")))
        );
    }
}