package ru.practicum.ewm.statsserver.commondto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

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
}