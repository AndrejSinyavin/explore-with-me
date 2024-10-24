package ru.practicum.ewm.statsserver.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@DisplayName("Тест запуска сервиса статистики")
class StatsServiceAppTest {

    @Test
    @DisplayName("Сервис запущен, сервер подключен к порту 9090")
    void mainTest() {
        var app = SpringApplication.run(StatsServiceApp.class);
        assertNotNull(app);
        assertTrue(app.isActive());
        assertTrue(app.isRunning());
        assertTrue(app.getEnvironment().containsProperty("server.port"));
        assertEquals("9090", app.getEnvironment().getProperty("server.port"));
    }
}