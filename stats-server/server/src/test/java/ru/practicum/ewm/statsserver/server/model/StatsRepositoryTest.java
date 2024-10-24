package ru.practicum.ewm.statsserver.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@Import({StatsServiceImpl.class})
@DisplayName("Набор интеграционных тестов для репозитория сервиса статистики")
public class StatsRepositoryTest {
    @Autowired
    private StatsService statsService;
    private EndpointHitEntity hitOne;

    public StatsRepositoryTest() {
    }

    @BeforeEach
    void setUp() {
        hitOne = new EndpointHitEntity(0L, "app", "uri", "ip", Instant.now(Clock.systemUTC()));
    }

    @Test
    @DisplayName("Запись в БД события 'успешная обработка обращения к эндпоинту'")
    void addHitTest() {
        var stats = statsService.getStats("0001-01-01 00:00:00", "9999-12-31 23:59:59", null, null);
        assertThat(stats.size(), is(0));
        statsService.add(hitOne);
        stats = statsService.getStats("0001-01-01 00:00:00", "9999-12-31 23:59:59", null, null);
        assertThat(stats.size(), is(1));
        assertThat(stats.getFirst(), is(notNullValue()));
        assertThat(stats.getFirst().hits(), is(1L));
        assertThat(stats.getFirst().uri(), is("uri"));
        assertThat(stats.getFirst().app(), is("app"));
    }
}