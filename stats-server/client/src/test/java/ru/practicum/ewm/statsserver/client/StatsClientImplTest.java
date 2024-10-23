package ru.practicum.ewm.statsserver.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.statsserver.commondto.EndpointHitCreateDto;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование клиента для работы с сервисом статистики")
class StatsClientImplTest {
    private final EndpointHitCreateDto correctData = new EndpointHitCreateDto(
            "correctApp",
            "correctUri",
            "correctIp",
            "correctDateTime");
    private final EndpointHitCreateDto incorrectData = new EndpointHitCreateDto("", "", "", "");

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    StatsClientImpl statsClient;

    @Test
    @DisplayName("Клиент отправил, а сервер статистики успешно записал информацию")
    void hitCorrectSaveDataTest() {
        Mockito
                .when(restTemplate.postForEntity(
                        "http://localhost:9090/hit", correctData, EndpointHitCreateDto.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        assertThat(statsClient.hit("correctApp", "correctUri", "correctIp", "correctDateTime"),
                is(true));
    }

    @Test
    @DisplayName("Сервер статистики не смог записать информацию, принятую от клиента")
    void hitIncorrectSaveDataTest() {
        Mockito
                .when(restTemplate.postForEntity(
                        "http://localhost:9090/hit", incorrectData, EndpointHitCreateDto.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(statsClient.hit("", "", "", ""),
                is(false));
    }

    @Test
    @DisplayName("Клиент не смог отправить запрос на сохранение информации серверу статистики")
    void hitDataSendingErrorTest() {
        Mockito
                .when(restTemplate.postForEntity(
                        "http://localhost:9090/hit", correctData, EndpointHitCreateDto.class))
                .thenThrow(RestClientException.class);
        assertThat(statsClient.hit("correctApp", "correctUri", "correctIp", "correctDateTime"),
                is(false));
    }

    @Test
    @DisplayName("Получение клиентом статистики с сервера")
    void getStatsNormalTest() {
        Mockito
                .when(restTemplate.getForObject("http://localhost:9090/stats", ViewStatsDto[].class))
                .thenReturn(new ViewStatsDto[]{new ViewStatsDto("app", "uri", 1L)});
        var result = statsClient.getStats();
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().get(0).app(), is("app"));
        assertThat(result.get().get(0).uri(), is("uri"));
        assertThat(result.get().get(0).hits(), is(1L));
    }

    @Test
    @DisplayName("Клиент вместо статистики получил в ответе null")
    void getStatsNullTest() {
        Mockito
                .when(restTemplate.getForObject("http://localhost:9090/stats", ViewStatsDto[].class))
                .thenReturn(null);
        assertThat(statsClient.getStats(), is(Optional.empty()));
    }

    @Test
    @DisplayName("Клиент не смог отправить на сервер запрос на получение статистики")
    void getStatsExceptionTest() {
        Mockito
                .when(restTemplate.getForObject("http://localhost:9090/stats", ViewStatsDto[].class))
                .thenThrow(RestClientException.class);
        assertThat(statsClient.getStats(), is(Optional.empty()));
    }
}