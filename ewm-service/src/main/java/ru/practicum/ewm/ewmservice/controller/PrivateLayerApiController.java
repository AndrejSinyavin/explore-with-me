package ru.practicum.ewm.ewmservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.exception.AppImproperDataException;
import ru.practicum.ewm.ewmservice.service.EwmService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateLayerApiController {
    String thisService = this.getClass().getSimpleName();
    static final String POSITIVE = "Эта величина может быть только положительным значением";
    static final String NOT_NEGATIVE = "Эта величина не может быть отрицательным значением";
    static final String UID = "user-id";
    static String SPLITTER = ". ";
    static String REQUEST_NOT_COMPLETE = "Запрос не выполнен";
    static String INVALID_DATA_SET = "Недопустимый набор данных в запросе";
    static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String GMT = "GMT0";
    static String POST_EVENT_REQUEST = "\n==>   Запрос POST: создать мероприятие {}";
    static String CREATED_EVENT_RESPONSE = "\n<==   Ответ: '201 Created' Запрос выполнен - создано мероприятие {}";

    EwmService ewmService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{user-id}/events")
    public EventFullDto createEvent(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Valid @RequestBody EventNewDto eventNewDto) {
        log.info(POST_EVENT_REQUEST, eventNewDto);
        var eventDateTime = Instant.from(LocalDateTime
                .parse(eventNewDto.eventDate(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                .atZone(ZoneId.of(GMT)));
        var now = Instant.now(Clock.systemUTC());
        if (eventDateTime.isBefore(now.plus(2L, HOURS))) {
            throw new AppImproperDataException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Дата начала события должна быть не ранее 2 часов от момента создания афиши"
                            .concat(" (минимальное время на модерацию)")
            );
        }
        var response = ewmService.addEvent(uId, eventNewDto, eventDateTime);
        log.info(CREATED_EVENT_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{user-id}/events")
    public List<EventFullDto> getEvents(

    ) {

    }

}
