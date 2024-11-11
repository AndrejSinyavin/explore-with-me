package ru.practicum.ewm.statsserver.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.statsserver.commondto.HitDto;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульное тестирование контроллера 'сервиса статистики'")
class StatsControllerTest {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private HitDto hitCreate;
    private String dateTime;

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    @BeforeEach
    void setUp() {
        dateTime = LocalDateTime.ofInstant(Instant.now(Clock.systemUTC()), UTC)
                .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        mvc = MockMvcBuilders.standaloneSetup(statsController).build();
        hitCreate = new HitDto(
                "app",
                "uri/normal",
                "200.125.12.94",
                dateTime
        );
    }

    @Test
    @DisplayName("Успешная запись корректных данных, ответ 201 Created")
    void hitCreateStatsWithCorrectRequestTest() throws Exception {
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(hitCreate))
                .characterEncoding("UTF-8")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated());
        verify(statsService).add(any());
    }

    @Test
    @DisplayName("Отсутствует тело в запросе, ответ 400 BadRequest")
    void hitCreateStatsWithNoBodyRequestTest() throws Exception {
        mvc.perform(post("/hit")
                        .characterEncoding("UTF-8")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение статистики при наличии всех аргументов, ответ 200 Ok")
    void getStatsWithAllArgTest() throws Exception {
        when(statsService.getStats(any(), any(), any(), any()))
                .thenReturn(List.of());
        mvc.perform(get("/stats")
                                .param("start", dateTime)
                                .param("end", dateTime)
                                .param("uris", List.of("uri").toString())
                                .param("unique", FALSE.toString())
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}