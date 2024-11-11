package ru.practicum.ewm.ewmservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CompilationDto;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventShortDto;
import ru.practicum.ewm.ewmservice.exception.EwmAppEntityNotFoundException;
import ru.practicum.ewm.ewmservice.service.EwmService;
import ru.practicum.ewm.statsserver.client.StatsClientImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Import({StatsClientImpl.class, RestTemplate.class})
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicLayerApiController {
    static final String POSITIVE = "Эта величина может быть только положительным значением";
    static final String NOT_NEGATIVE = "Эта величина не может быть отрицательным значением";
    static final String CID = "cat-id";
    static final String EID = "event-id";
    static final String CPID = "comp-id";
    static final String SIZE = "size";
    static final String FROM = "from";
    static final String FROM_DEFAULT = "0";
    static final String SIZE_DEFAULT = "10";
    static String REQUEST_NOT_COMPLETE = "Запрос не выполнен";
    static String ENTITY_NOT_FOUND = "Сущность не найдена";
    static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String GET_CATEGORY_REQUEST = "\n==>   Запрос GET: получить категорию события по ID {} ";
    static String GET_CATEGORIES_REQUEST =
            "\n==>   Запрос GET: получить список категорий в диапазоне pageFrom {} pageSize {}";
    static String GET_CATEGORY_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - запрошенный пользователь: {}";
    static String GET_CATEGORIES_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - запрошенные категории: {}";
    static String GET_PUBLISHED_EVENT = "\n==>   Запрос GET: получить подробную информацию о событии ID {}";
    static String PUBLISHED_EVENT = "\n<==   Ответ: '200 Ok' Запрос выполнен - событие: {}";
    static String SEND_ACTION_TO_STAT_SERVICE =
            "\n<==    Информация о просмотре события отправлена сервису статистики: {} {} {}";
    static String GET_EVENTS_BY_CRITERIA = "\n==>   Запрос GET: получить список опубликованных событий по критериям {}";
    static String EVENTS_BY_CRITERIA = "\n<==   Ответ: '200 Ok' Запрос выполнен - список событий: {}";

    String thisService = this.getClass().getName();
    StatsClientImpl statsClient;
    EwmService ewmService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/categories/{cat-id}")
    public CategoryDto getCategory(@Positive(message = POSITIVE) @PathVariable(value = CID) Long cId) {
        log.info(GET_CATEGORY_REQUEST, cId);
        var response = ewmService.getCategoryById(cId);
        log.info(GET_CATEGORY_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @PositiveOrZero(message = NOT_NEGATIVE) @RequestParam(value = FROM, defaultValue = FROM_DEFAULT)
            Integer pageFrom,
            @Positive(message = POSITIVE) @RequestParam(value = SIZE, defaultValue = SIZE_DEFAULT)
            Integer pageSize) {
        log.info(GET_CATEGORIES_REQUEST, pageFrom, pageSize);
        var response = ewmService.getCategories(pageFrom, pageSize);
        log.info(GET_CATEGORIES_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/events/{event-id}")
    public EventFullDto getEvent(
            @Positive(message = POSITIVE) @PathVariable(value = EID) Long eId,
            HttpServletRequest request
    ) {
        log.info(GET_PUBLISHED_EVENT, eId);
        var response = ewmService.getFullEvent(eId);
        log.info(PUBLISHED_EVENT, response);
        String endpointPath = request.getRequestURI();
        String ip = request.getRemoteAddr();
        var isUniqueHit = logAction(thisService, endpointPath, ip);
        if (isUniqueHit) {
            ewmService.addReview(eId);
        }
        log.info("Просмотр афиши ID {} зафиксирован в сервисе статистики, он уникальный: {}", eId, isUniqueHit);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/events")
    public List<EventShortDto> getEvents(HttpServletRequest request, @RequestParam Map<String, String> params
    ) {
        log.info(GET_EVENTS_BY_CRITERIA, params);
        var response = ewmService.getEventsByCriteria(request, params);
        log.info(EVENTS_BY_CRITERIA, response);
        log.info(PUBLISHED_EVENT, response);
        String endpointPath = request.getRequestURI();
        String ip = request.getRemoteAddr();
        logAction(thisService, endpointPath, ip);
        return response;
    }

    private boolean logAction(String service, String endpointPath, String ip) {
        log.info(SEND_ACTION_TO_STAT_SERVICE, service, endpointPath, ip);
        return statsClient.hit(
                service,
                endpointPath,
                ip,
                LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/compilations/{comp-id}")
    public CompilationDto getCompilation(
            @Positive(message = POSITIVE) @PathVariable(value = CPID) Long cpId
    ) {
        return ewmService.getCompilationById(cpId)
                .orElseThrow(() ->
                        new EwmAppEntityNotFoundException(
                                REQUEST_NOT_COMPLETE, ENTITY_NOT_FOUND,
                                "Не найдена компиляция ID ".concat(String.valueOf(cpId))
                        )
                );
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(
            @RequestParam(value = "pinned", defaultValue = "false") Boolean pinned,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        return ewmService.getCompilations(pinned, from, size);
    }

}
