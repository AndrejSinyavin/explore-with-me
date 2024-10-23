package ru.practicum.ewm.statsserver.commondto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * DTO возвращает статистику по обработке запросов пользователей к эндпоинтам API приложения
 *
 * @param app компонент, обработавший запрос
 * @param uri на какой эндпоинт был запрос
 * @param hits количество сделанных запросов, подсчитывается на основании параметров в сервисном запросе
 *            на получение статистики
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) implements Serializable {
}
