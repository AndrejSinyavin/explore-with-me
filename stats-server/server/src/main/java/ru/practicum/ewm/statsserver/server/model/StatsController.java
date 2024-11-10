package ru.practicum.ewm.statsserver.server.model;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.statsserver.commondto.HitDto;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;

import java.util.List;

/**
 * Контроллер обработки REST-запросов на API сервиса статистики
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
@RestController
public class StatsController {
    static String POST_REQUEST = "Запрос POST: сохранить статистику {}";
    static String GET_REQUEST = "Запрос GET: получить статистику по набору критериев: {} {} {} {}";
    static String OK_RESPONSE = "Ok 200 ";
    static String CREATE_RESPONSE = "Created 201 ";
    StatsService statsService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public Boolean hit(@Valid @RequestBody HitDto dto) {
        log.info(POST_REQUEST, dto);
        boolean isUnique = statsService.add(dto);
        log.info(CREATE_RESPONSE);
        return isUnique;
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") boolean unique
    ) {
        log.info(GET_REQUEST, start, end, uris, unique);
        var result = statsService.getStats(start, end, uris, unique);
        log.info(OK_RESPONSE.concat(result.toString()));
        return result;
    }
}