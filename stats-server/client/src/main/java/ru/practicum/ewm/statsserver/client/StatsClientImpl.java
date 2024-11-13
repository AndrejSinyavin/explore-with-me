package ru.practicum.ewm.statsserver.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.statsserver.commondto.HitDto;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StatsClientImpl {
    private final RestClient restClient = RestClient.create();
    @Value("${stats-server.url:http://localhost:9090}")
    private String statServerUrl;

    public boolean hit(String app, String uri, String ip, String timestamp) {
        String hitUri = UriComponentsBuilder.fromHttpUrl(statServerUrl.concat("/hit")).toUriString();
        try {
            var hitCreateDto = new HitDto(app, uri, ip, timestamp);
            return Boolean.TRUE.equals(
                        restClient.post()
                            .uri(hitUri)
                            .acceptCharset(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(hitCreateDto)
                            .retrieve()
                            .body(Boolean.class)
            );
        } catch (RuntimeException exception) {
            log.info(this.getClass().getName()
                .concat("\nОшибка клиента при отправке статистики: ").concat(exception.getMessage()));
            return false;
        }
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        String getStatsUri = UriComponentsBuilder.fromHttpUrl(statServerUrl.concat("/stats")).toUriString();
        var request = UriComponentsBuilder
                .fromHttpUrl(getStatsUri)
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParamIfPresent("uris", Optional.ofNullable(uris))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .build().encode(StandardCharsets.UTF_8).toUri();
        return restClient.get()
                .uri(request)
                .acceptCharset(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ParameterizedTypeReference.forType(ViewStatsDto.class));
    }

}
