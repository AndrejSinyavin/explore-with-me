package ru.practicum.ewm.statsserver.commondto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * DTO для регистрации события 'Обработан запрос клиента на эндпоинт API приложения'
 *
 * @param app компонент, обработавший запрос
 * @param uri на какой эндпоинт был запрос
 * @param ip с какого IP был выполнен запрос
 * @param timestamp дата и время выполнения запроса
 */
public record HitDto(
        @NotBlank
        String app,
        @NotBlank
        String uri,
        @NotBlank
        String ip,
        @NotBlank
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        String timestamp
) implements Serializable {
}