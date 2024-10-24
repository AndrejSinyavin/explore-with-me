package ru.practicum.ewm.ewmservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = EwmServiceApp.class)
@DisplayName("Тест запуска основного сервиса")
class EwmServiceAppTest {

    @Test
    @DisplayName("Сервис запущен")
    void mainTest() {
        var app = SpringApplication.run(EwmServiceApp.class);
        assertNotNull(app);
        assertTrue(app.isActive());
        assertTrue(app.isRunning());
    }
}