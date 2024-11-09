package ru.practicum.ewm.statsserver.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;
import ru.practicum.ewm.statsserver.server.exception.AppBadRequestException;
import ru.practicum.ewm.statsserver.server.exception.AppInternalServiceException;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование сервисного слоя 'сервиса статистики'")
class StatsServiceImplTest {
    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsServiceImpl;

    private EndpointHitEntity endpointHitEntity;

    @BeforeEach
    void setUp() {
        endpointHitEntity = new EndpointHitEntity(0L, "", "", "", Instant.now(Clock.systemUTC()));
    }

    @Test
    @DisplayName("Сценарий тестирования логики метода добавления статистики")
    void addTest() {
        when(statsRepository.save(any()))
                .thenThrow(RuntimeException.class);
        assertThrows(AppInternalServiceException.class,
                () -> statsServiceImpl.add(endpointHitEntity));
        verify(statsRepository).save(any());
    }

    @Test
    @DisplayName("Сценарий тестирования логики метода получения статистики для всех эндпоинтов без уникальных IP")
    void getStatsAllWithoutUniqueTest() {
        when(statsRepository.getStatsWithoutUris(any(), any()))
                .thenReturn(new ArrayList<>());
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                null,
                null), is(anything()));
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                null,
                false), is(anything()));
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                List.of(""),
                false), is(anything()));
    }

    @Test
    @DisplayName("Сценарий тестирования логики метода получения статистики для всех эндпоинтов c учетом уникальных IP")
    void getStatsWithEndpointsWithoutUniqueTest() {
        when(statsRepository.getStatsWithoutUrisAndWithUnique(any(), any()))
                .thenReturn(new ArrayList<>(Collections.singleton(new ViewStatsDto("app", "uri", 1L))));
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                null,
                true), is(anything()));
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                List.of(""),
                true), is(anything()));
    }

    @Test
    @DisplayName("Сценарий тестирования логики метода получения статистики для выборочных эндпоинтов без уникальных IP")
    void getStatsWithUrisWithoutUniqueTest() {
        when(statsRepository.getStatsWithUris(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                List.of("uri"),
                null), is(anything()));
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                List.of("uri"),
                false), is(anything()));
    }

    @Test
    @DisplayName("Сценарий тестирования логики метода получения статистики для выборочных эндпоинтов c уникальными IP")
    void getStatsWithUrisWithUniqueTest() {
        when(statsRepository.getStatsWithUrisAndWithUnique(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        assertThat(statsServiceImpl.getStats(
                "2000-01-01 00:00:00",
                "2000-01-01 00:00:00",
                List.of("uri"),
                true), is(anything()));
    }

    @Test
    @DisplayName("Сценарий тестирования обработки исключений при ошибках в работе метода")
    void getStatsExceptionsTest() {
        when(statsRepository.getStatsWithoutUris(any(), any()))
                .thenThrow(RuntimeException.class);
        assertThrows(AppBadRequestException.class,
                () -> statsServiceImpl.getStats(
                        "2000-01-01 00:00:00",
                        "2000-01-01 00:00:00",
                        null,
                        null));
    }

}