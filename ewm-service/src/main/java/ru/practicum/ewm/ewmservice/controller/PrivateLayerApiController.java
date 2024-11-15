package ru.practicum.ewm.ewmservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByUserRequestDto;
import ru.practicum.ewm.ewmservice.dto.ParticipationRequestDto;
import ru.practicum.ewm.ewmservice.service.EwmService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateLayerApiController {
    static final String POSITIVE = "Эта величина может быть только положительным значением";
    static final String NOT_NEGATIVE = "Эта величина не может быть отрицательным значением";
    static final String UID = "user-id";
    static final String EID = "event-id";
    static final String RID = "request-id";
    static final String SIZE = "size";
    static final String FROM = "from";
    static final String EVID = "eventId";
    static final String FROM_DEFAULT = "0";
    static final String SIZE_DEFAULT = "10";
    static String CREATE_EVENT = "\n==>   Запрос POST: создать мероприятие {}";
    static String CREATED_EVENT = "\n<==   Ответ: '201 Created' Запрос выполнен - создано мероприятие {}";
    static String GET_EVENTS =
            "\n==>   Запрос GET: получить список мероприятий пользователя ID {}, в диапазоне: pageFrom {} pageSize {}";
    static String USERS_EVENTS =
            "\n<==   Ответ: '200 Ok' Запрос выполнен - список запрошенных мероприятий пользователей: {}";
    static String GET_EVENT = "\n==>   Запрос GET: получить мероприятие ID {} для пользователя ID {}";
    static String USERS_EVENT = "\n<==   Ответ: '200 Ok' Запрос выполнен - пользователь: ID {}, мероприятие {}";
    static String UPDATE_EVENT = "\n==>   Запрос PATCH: обновить мероприятие ID {} пользователя ID {} {}";
    static String UPDATED_EVENT = "\n<==   Ответ: '200 Ok' Запрос выполнен - мероприятие обновлено: {}";
    static String CREATE_REQUEST =
            "\n==>   Запрос POST: создать заявку на участие в мероприятии ID {} от пользователя ID {}";
    static String REQUEST_CREATED = "\n<==   Ответ: '201 Create' Запрос выполнен - заявка создана: {}";
    static String CANCEL_REQUEST = "\n==>   Запрос PATCH: отменить заявку ID {} от пользователя ID {}";
    static String REQUEST_CANCELED = "\n<==   Ответ: '200 Ok' Запрос выполнен - заявка отменена";
    static String CHANGE_REQUESTS_STATUSES =
            "\n==>   Запрос PATCH: изменить статус заявок на участие в событии {} пользователя {}: {}";
    static String REQUESTS_STATUSES_CHANGED =
            "\n<==   Ответ: '200 Ok' Запрос выполнен - статус заявок обновлен {}";
    static String FIND_REQUESTS_FOR_MY_EVENT =
            "\n==>   Запрос GET: получить список запросов на участие в мероприятии ID {} пользователя ID {}";
    static String LIST_REQUESTS_FOR_MY_EVENT =
        "\n<==   Ответ: '200 Ok' Запрос выполнен - список запросов на участие в мероприятии ID {} пользователя {}: {}";
    static String FIND_MY_REQUESTS_FOR_ANY_EVENTS =
            "\n==>   Запрос GET: получить список всех запросов пользователя ID {} на участие в мероприятиях";
    static String MY_REQUESTS_FUNDED =
            "\n<==   Ответ: '200 Ok' Запрос выполнен - список запросов на участие пользователя {} в мероприятиях: {}";
    static String SET_EXPECTATION_RATING =
            "\n==>   Запрос POST:  рейтинг мероприятия - пользователя ID {} интересует предстоящие мероприятие ID {}";
    static String EXPECTATION_RATING_CREATED =
            "\n<==   Ответ: '201 Create' Запрос выполнен - пользователь отметил событие как интересное";
    static String SET_SATISFACTION_RATING =
            "\n==>   Запрос POST:  рейтинг мероприятия - пользователь ID {} оценил мероприятие ID {} оценкой {}";
    static String SATISFACTION_RATING_CREATED = "\n<==   Ответ: '201 Create' Запрос выполнен - оценка принята";
    static final String PathForUsersEvent = "/{user-id}/events/{event-id}";

    EwmService ewmService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{user-id}/events")
    public EventFullDto addEvent(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Valid @RequestBody EventNewDto eventNewDto) {
        log.info(CREATE_EVENT, eventNewDto);
        var response = ewmService.addEvent(uId, eventNewDto);
        log.info(CREATED_EVENT, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{user-id}/events")
    public List<EventFullDto> getEvents(
            @Positive(message = POSITIVE) @PathVariable(UID)
            Long uId,
            @PositiveOrZero(message = NOT_NEGATIVE) @RequestParam(value = FROM, defaultValue = FROM_DEFAULT)
            Integer pageFrom,
            @Positive(message = POSITIVE) @RequestParam(value = SIZE, defaultValue = SIZE_DEFAULT)
            Integer pageSize
    ) {
        log.info(GET_EVENTS, uId, pageFrom, pageSize);
        var response = ewmService.getEvents(uId, pageFrom, pageSize);
        log.info(USERS_EVENTS, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(PathForUsersEvent)
    public EventFullDto getEvent(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId
    ) {
        log.info(GET_EVENT, eId, uId);
        var response = ewmService.getEventByIdAndUserId(eId, uId);
        log.info(USERS_EVENT, uId, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(PathForUsersEvent)
    public EventFullDto updateEvent(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId,
            @Valid @RequestBody EventUpdateByUserRequestDto eventPatchDto
    ) {
        log.info(UPDATE_EVENT, eId, uId, eventPatchDto);
        var response = ewmService.authorUpdateEvent(uId, eId, eventPatchDto);
        log.info(UPDATED_EVENT, response);
        return response;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{user-id}/requests")
    public ParticipationRequestDto createRequest(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = NOT_NEGATIVE) @RequestParam(EVID) Long eId
    ) {
        log.info(CREATE_REQUEST, eId, uId);
        var response = ewmService.createParticipationRequest(uId, eId);
        log.info(REQUEST_CREATED, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(PathForUsersEvent + "/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId,
            @RequestBody EventRequestStatusUpdateRequest statuses
    ) {
        log.info(CHANGE_REQUESTS_STATUSES, eId, uId, statuses);
        var response = ewmService.updateRequestStatuses(statuses, uId, eId);
        log.info(REQUESTS_STATUSES_CHANGED, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{user-id}/requests/{request-id}/cancel")
    public ParticipationRequestDto cancelRequest(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(RID) Long rId
    ) {
        log.info(CANCEL_REQUEST, rId, uId);
        var response = ewmService.cancelRequest(rId, uId);
        log.info(REQUEST_CANCELED);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(PathForUsersEvent + "/requests")
    public List<ParticipationRequestDto> getRequestsForUserEvent(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId
    ) {
        log.info(FIND_REQUESTS_FOR_MY_EVENT, eId, uId);
        var response = ewmService.getRequestsForUserEvent(eId, uId);
        log.info(LIST_REQUESTS_FOR_MY_EVENT, eId, uId, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{user-id}/requests")
    public List<ParticipationRequestDto> getAllUserRequests(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId
    ) {
        log.info(FIND_MY_REQUESTS_FOR_ANY_EVENTS, uId);
        var response = ewmService.getAllUserRequests(uId);
        log.info(MY_REQUESTS_FUNDED, uId, response);
        return response;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{user-id}/expectations/{event-id}")
    public void addEventExpectationRating(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId
    ) {
        log.info(SET_EXPECTATION_RATING, uId, eId);
        ewmService.addEventExpectationRating(uId, eId);
        log.info(EXPECTATION_RATING_CREATED);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{user-id}/satisfactions/{event-id}")
    public void addEventSatisfactionRating(
            @Positive(message = POSITIVE) @PathVariable(UID) Long uId,
            @Positive(message = POSITIVE) @PathVariable(EID) Long eId,
            @RequestParam(value = "rating", defaultValue = "10") String rating
    ) {
        log.info(SET_SATISFACTION_RATING, uId, eId, rating);
        ewmService.addEventSatisfactionRating(uId, eId, rating);
        log.info(SATISFACTION_RATING_CREATED);
    }
}
