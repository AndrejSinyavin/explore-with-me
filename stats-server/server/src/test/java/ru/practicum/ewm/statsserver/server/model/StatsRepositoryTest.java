package ru.practicum.ewm.statsserver.server.model;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DisplayName("Набор интеграционных тестов для репозитория сервиса статистики")
public class StatsRepositoryTest {
    private final StatsRepository statsRepository;
    private final StatsService statsService;
    private EndpointHitEntity hitOne;
    private EndpointHitEntity hitTwo;
    private EndpointHitEntity hitThree;
    private Instant now;

    public StatsRepositoryTest() throws InstantiationException, IllegalAccessException {
    }

    @BeforeEach
    void setUp() {
        now = Instant.now(Clock.systemUTC());
        hitOne = new EndpointHitEntity(0L, "app", "uri", "ip", now);
        hitTwo = new EndpointHitEntity(0L, "app-1", "uri-1", "ip-1", now);
        hitThree = new EndpointHitEntity(0L, "app-2", "uri-2", "ip-2", now);
    }

    @Test
    @DisplayName("Запись в БД события 'успешная обработка обращения к эндпоинту'")
    void addHitTest() {
        var stats = statsService.getStats("0000-00-00 00:00:00", "9999-12-31 23:59:59", null, null);
        assertThat(stats.size(), is(0));
        statsService.add(hitOne);
        stats = statsService.getStats("0000-00-00 00:00:00", "9999-12-31 23:59:59", null, null);
        assertThat(stats.size(), is(1));
        assertThat(stats.getFirst(), is(notNullValue()));
        assertThat(stats.getFirst(), is(hitOne));
    }
}