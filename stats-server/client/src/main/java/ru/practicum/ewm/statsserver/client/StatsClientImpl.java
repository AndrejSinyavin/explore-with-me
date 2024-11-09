package ru.practicum.ewm.statsserver.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${stats-server.url}")
    private String statServerUrl;
    private final RestTemplate restTemplate;

    public Boolean hit(String app, String uri, String ip, String timestamp) {
        try {
            var hitCreateDto = new EndpointHitCreateDto(app, uri, ip, timestamp);
            ResponseEntity<EndpointHitCreateDto> responseEntity =
                    restTemplate.postForEntity(
                            statServerUrl.concat("/hit"), hitCreateDto, EndpointHitCreateDto.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
                return true;
            } else if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                return false;
            } else {
                throw new RestClientException("От сервиса статистики получен ответ, не соответсвующий протоколу");
            }
        } catch (RestClientException e) {
            log.info(this.getClass().getName()
                .concat(" Ошибка клиента при отправке статистики!"));
            return false;
        }
    }

    public Optional<List<ViewStatsDto>> getStats() {
        try {
            ViewStatsDto[] stats = restTemplate.getForObject(statServerUrl.concat("/stats"), ViewStatsDto[].class);
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

    public void setStatServerUrl(String statServerUrl) {
        this.statServerUrl = statServerUrl;
    }

}
