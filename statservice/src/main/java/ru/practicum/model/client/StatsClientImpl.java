package ru.practicum.model.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.model.dto.EndpointHitCreateDto;
import ru.practicum.model.dto.ViewStatsDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class StatsClientImpl {
    @Value(value = "http://localhost:${server.port}/")
    private String statsServiceUri;
    private final RestTemplate restTemplate = new RestTemplate();

    public void hit(String app, String uri, String ip, String timestamp) {
        var hitCreateDto = new EndpointHitCreateDto(app, uri, ip, timestamp);
        ResponseEntity<EndpointHitCreateDto> responseEntity =
                restTemplate.postForEntity(statsServiceUri, hitCreateDto, EndpointHitCreateDto.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("Регистрация запроса пользователя не выполнена, сбой сервиса статистики!");
        }
    }

    public List<ViewStatsDto> getStats() {
        ViewStatsDto[] stats = restTemplate.getForObject(statsServiceUri, ViewStatsDto[].class);
        if (stats == null) {
            log.warn("Статистика отсутствует!");
            return new ArrayList<>();
        } else {
            return Arrays.stream(stats).toList();
        }
    }

}
