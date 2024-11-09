package ru.practicum.ewm.statsserver.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;

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
    }
}