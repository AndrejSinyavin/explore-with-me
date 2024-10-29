package ru.practicum.ewm.statsserver.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.statsserver.commondto.EndpointHitCreateDto;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StatsClientImpl {
    private static final String STATS_SERVICE_URI = "http://localhost:9090/";
    private final RestTemplate restTemplate;

    public Boolean hit(String app, String uri, String ip, String timestamp) {
        try {
            var hitCreateDto = new EndpointHitCreateDto(app, uri, ip, timestamp);
            ResponseEntity<EndpointHitCreateDto> responseEntity =
                    restTemplate.postForEntity(
                            STATS_SERVICE_URI.concat("hit"), hitCreateDto, EndpointHitCreateDto.class);
            var status = true;
            if (!responseEntity.getStatusCode().isSameCodeAs(HttpStatus.CREATED)) {
                log.info(" Регистрация запроса пользователя не выполнена, ошибка в работе сервиса статистики! {}",
                        this.getClass().getName());
                status = false;
            }
            return status;
        } catch (RestClientException e) {
            log.info(this.getClass().getName()
                .concat(" Регистрация запроса пользователя не выполнена, ошибка клиента при отправке статистики!"));
            return false;
        }
    }

    public Optional<List<ViewStatsDto>> getStats() {
        try {
            ViewStatsDto[] stats = restTemplate.getForObject(STATS_SERVICE_URI.concat("stats"), ViewStatsDto[].class);
            if (stats == null) {
                log.warn(this.getClass().getName()
                        .concat(" Ошибка сервера: cтатистика отсутствует!"));
                return Optional.empty();
            } else {
                return Optional.of(Arrays.stream(stats).toList());
            }
        } catch (RestClientException e) {
            log.warn(this.getClass().getName()
                    .concat(" Ошибка клиента при отправке запроса на получение статистики!"));
            return Optional.empty();
        }
    }

}
