package ru.practicum.model.server;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.model.dto.EndpointHitCreateDto;
import ru.practicum.model.dto.ViewStatsDto;

import java.util.List;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
public class StatsController {
    static String POST_REQUEST = "Запрос POST: сохранить статистику {}";
    static String GET_REQUEST = "Запрос GET: получить статистику по набору критериев: {} {} {} {}";
    static String OK_RESPONSE = "Ok 200 ";
    static String CREATE_RESPONSE = "Ok 201 ";
    StatsService statsService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void hit(@RequestBody EndpointHitCreateDto dto) {
        log.info(POST_REQUEST, dto);
        statsService.add(dto.toEntity());
        log.info(CREATE_RESPONSE);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam(value = "start") String start,
            @RequestParam(value = "end") String end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", required = false) Boolean unique
    ) {
        log.info(GET_REQUEST, start, end, uris, unique);
        var result = statsService.getStats(start, end, uris, unique);
        log.info(OK_RESPONSE.concat(result.toString()));
        return result;
    }

}