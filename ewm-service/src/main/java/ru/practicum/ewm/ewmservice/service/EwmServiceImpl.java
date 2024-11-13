package ru.practicum.ewm.ewmservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CategoryNewDto;
import ru.practicum.ewm.ewmservice.dto.CompilationDto;
import ru.practicum.ewm.ewmservice.dto.CompilationNewDto;
import ru.practicum.ewm.ewmservice.dto.CompilationUpdateRequestDto;
import ru.practicum.ewm.ewmservice.dto.EventRateDto;
import ru.practicum.ewm.ewmservice.dto.EventsStatsDto;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.ewmservice.dto.EventShortDto;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByAdminRequestDto;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByUserRequestDto;
import ru.practicum.ewm.ewmservice.dto.LocationDto;
import ru.practicum.ewm.ewmservice.dto.ParticipationRequestDto;
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState;
import ru.practicum.ewm.ewmservice.entity.CategoryEntity;
import ru.practicum.ewm.ewmservice.entity.CompilationEntity;
import ru.practicum.ewm.ewmservice.entity.CompilationEventRelation;
import ru.practicum.ewm.ewmservice.entity.EventEntity;
import ru.practicum.ewm.ewmservice.entity.EventExpectationRatingEntity;
import ru.practicum.ewm.ewmservice.entity.EventLocationEntity;
import ru.practicum.ewm.ewmservice.entity.EventSatisfactionRatingEntity;
import ru.practicum.ewm.ewmservice.entity.EventState;
import ru.practicum.ewm.ewmservice.entity.EventStatsEntity;
import ru.practicum.ewm.ewmservice.entity.UserRequestModerationState;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestEntity;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestState;
import ru.practicum.ewm.ewmservice.entity.UserEntity;
import ru.practicum.ewm.ewmservice.exception.EwmAppEntityNotFoundException;
import ru.practicum.ewm.ewmservice.exception.EwmAppRequestValidateException;
import ru.practicum.ewm.ewmservice.exception.EwmAppConflictActionException;
import ru.practicum.ewm.ewmservice.repository.CategoryRepository;
import ru.practicum.ewm.ewmservice.repository.CompilationEventRelationRepository;
import ru.practicum.ewm.ewmservice.repository.CompilationRepository;
import ru.practicum.ewm.ewmservice.repository.EventLocationRepository;
import ru.practicum.ewm.ewmservice.repository.EventRepository;
import ru.practicum.ewm.ewmservice.repository.RatingExpectationRepository;
import ru.practicum.ewm.ewmservice.repository.RatingSatisfactionRepository;
import ru.practicum.ewm.ewmservice.repository.RatingStatRepository;
import ru.practicum.ewm.ewmservice.repository.ParticipationRequestRepository;
import ru.practicum.ewm.ewmservice.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState.PUBLISH_EVENT;
import static ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState.REJECT_EVENT;
import static ru.practicum.ewm.ewmservice.entity.EventState.*;
import static ru.practicum.ewm.ewmservice.entity.ParticipationRequestState.CONFIRMED;
import static ru.practicum.ewm.ewmservice.entity.ParticipationRequestState.REJECTED;
import static ru.practicum.ewm.ewmservice.entity.UserRequestModerationState.CANCEL_REVIEW;
import static ru.practicum.ewm.ewmservice.entity.UserRequestModerationState.SEND_TO_REVIEW;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EwmServiceImpl implements EwmService {
    String thisService = this.getClass().getSimpleName();
    static int AUTHORS_ACTION_MODERATION_LIMIT = 2;
    static int ADMINS_ACTION_MODERATION_LIMIT = 1;
    static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String GMT = "GMT0";
    static String SPLITTER = ". ";
    static String REQUEST_NOT_COMPLETE = "Запрос не выполнен";
    static String ENTITY_NOT_FOUND = "Сущность не найдена";
    static String CONFLICT = "Не соблюдаются необходимые условия для выполнения запроса";
    static String CATEGORY_NOT_FOUND = "В репозитории не найдена категория мероприятия с ID: ";
    static String REQUEST_NOT_FOUND = "В репозитории не найдена заявка на участие в мероприятии, заявка ID: ";
    static String INVALID_DATA_SET = "Недопустимый набор данных в запросе";
    EntityManager entityManager;
    UserRepository userRepository;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    EventLocationRepository eventLocationRepository;
    ParticipationRequestRepository participationRequestRepository;
    CompilationEventRelationRepository compilationEventRelationRepository;
    CompilationRepository compilationRepository;
    RatingExpectationRepository ratingExpectationRepository;
    RatingSatisfactionRepository ratingSatisfactionRepository;
    RatingStatRepository ratingStatRepository;


    /**
     * Создание пользователя
     * @param userNewDto - данные для создания
     * @return созданная анкета с идентификатором
     */
    @Override
    public UserDto addUser(UserNewDto userNewDto) {
        return userRepository.save(userNewDto.toEntity()).toUserDto();
    }

    /**
     * Найти пользователя(ей) по набору критериев
     * @param criteria критерии поиска
     * @return список найденных пользователей
     */
    @Override
    public List<UserDto> getUsers(SearchCriteria criteria) {
        var listIntIds = criteria.ids();
        if (listIntIds != null && listIntIds.length > 0) {
            List<Long> listLongIds = Arrays.stream(listIntIds)
                    .mapToLong(Integer::longValue).boxed().sorted().toList();
            return userRepository.getAllByIdInOrderById(listLongIds);
        } else {
            return userRepository.getPageOrderedByIdAsc(criteria.pageSize(), criteria.pageFrom());
        }
    }

    /**
     * Удаление пользователя с сервиса
     * @param uId идентификатор пользователя
     */
    @Override
    public void deleteUser(Long uId) {
        if (!userRepository.existsById(uId)) {
            throw new EwmAppEntityNotFoundException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                    "В репозитории не найден пользователь с ID: ".concat(String.valueOf(uId))
            );
        }
        userRepository.deleteById(uId);
    }

    /**
     * Создание новой категории для события
     * @param categoryNewDto данные для создания
     * @return созданная категория с идентификатором
     */
    @Override
    public CategoryDto addCategory(CategoryNewDto categoryNewDto) {
        return categoryRepository.save(categoryNewDto.toEntity()).toCategoryDto();
    }

    /**
     * Получение категории события по ее идентификатору
     * @param cId идентификатор события
     * @return найденное событие
     */
    @Override
    public CategoryDto getCategoryById(Long cId) {
        return getCategory(cId).toCategoryDto();
    }

    /**
     * Получить для просмотра группу категорий события
     * @param limit количество
     * @param offset с какого порядкового номера в существующем наборе
     * @return
     */
    @Override
    public List<CategoryDto> getCategories(int offset, int limit) {
        return categoryRepository.getPageOrderedByIdAsc(offset, limit);
    }

    /**
     * Удалить категорию события
     * @param cId идентификатор категорию
     */
    @Override
    public void deleteCategoryById(Long cId) {
        checkCategoryById(cId);
        categoryRepository.deleteById(cId);
    }

    /**
     * Выборочно обновить поля категории события
     * @param cId идентификатор категории
     * @param categoryNewDto набор полей
     * @return обновленная категория
     */
    @Override
    public CategoryDto updateCategoryById(Long cId, CategoryNewDto categoryNewDto) {
        categoryRepository.updateNameById(categoryNewDto.name(), cId);
        return categoryRepository.findById(cId).orElseThrow(
                () -> new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(cId))
                ))
                .toCategoryDto();
    }

    /**
     * Добавить анкету события на сервис. После добавления имеет статус ожидания модерации.
     * @param uId идентификатор автора анкеты
     * @param newEvent данные о событии
     * @return анкета с установленным идентификатором
     */
    @Transactional
    @Override
    public EventFullDto addEvent(Long uId, EventNewDto newEvent) {
        var eventDateTime = toInstantTime(newEvent.eventDate());
        checkActionTimeLimitBeforePublication(
                Instant.now(Clock.systemUTC()), eventDateTime, AUTHORS_ACTION_MODERATION_LIMIT);
        var eventCreatorUser = userRepository
                .findById(uId)
                .orElseThrow(() -> new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        "В репозитории не найден пользователь с ID: ".concat(String.valueOf(uId)))
                );
        var categoryId = newEvent.category();
        var eventCategory = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(categoryId)))
                );
        EventLocationEntity eventLocation = new EventLocationEntity();
        eventLocation.setLat(newEvent.location().lat());
        eventLocation.setLon(newEvent.location().lon());
        eventLocation = eventLocationRepository.save(eventLocation);
        EventEntity createdEvent = new EventEntity();
        createdEvent.setAnnotation(newEvent.annotation());
        createdEvent.setCategoryEntity(eventCategory);
        createdEvent.setDescription(newEvent.description());
        createdEvent.setEventDate(eventDateTime);
        createdEvent.setLocation(eventLocation);
        createdEvent.setTitle(newEvent.title());
        createdEvent.setConfirmedRequests(0L);
        createdEvent.setCreatedOn(Instant.now(Clock.systemUTC()));
        createdEvent.setInitiator(eventCreatorUser);
        createdEvent.setViews(0L);
        if (newEvent.paid() != null) {
            createdEvent.setPaid(newEvent.paid());
        } else {
            createdEvent.setPaid(false);
        }
        if (newEvent.participantLimit() != null) createdEvent.setParticipantLimit(newEvent.participantLimit());
        if (newEvent.requestModeration() != null) createdEvent.setRequestModeration(newEvent.requestModeration());
        createdEvent.setState(EventState.PENDING);
        var event = eventRepository.save(createdEvent);
        var eventStats = new EventStatsEntity();
        eventStats.setEvent(event);
        eventStats.setExpectationRate(0L);
        eventStats.setSummarySatisfactionRate(0L);
        ratingStatRepository.save(eventStats);
        return event.toEventFullDto();
    }

    /**
     * Показать анкеты событий одного автора в диапазоне
     * @param uId идентификатор автора
     * @param pageFrom с позиции в списке
     * @param pageSize размер выборки
     * @return список для просмотра
     */
    @Override
    public List<EventFullDto> getEvents(Long uId, Integer pageFrom, Integer pageSize) {
        return eventRepository.findByInitiator_IdOrderByIdAsc(uId, pageFrom, pageSize);
    }

    /**
     * Просмотреть конкретную афишу событий указанного автора
     * @param eId идентификатор события
     * @param uId идентификатор автора
     * @return найденная афиша
     */
    @Override
    public EventFullDto getEventByIdAndUserId(Long eId, Long uId) {
        return eventRepository.findByIdAndInitiator_Id(eId, uId).orElseThrow(() ->
                new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        String.format("В репозитории не найдено событие ID %d для пользователя ID %d", eId, uId)
                )).toEventFullDto();
    }

    /**
     * Запрос с данными на обновление афиши от ее автора на модерацию. Сценарии проверки на валидность запроса, его
     * регистрация или отказ.
     * @param uId идентификатор события
     * @param eId идентификатор автора
     * @param userRequestToUpdateEvent набор данных для обновления и запрос на публикацию, или снятие с модерации
     * @return обновленная анкета, сохраненная на сервисе, ожидающая публикации
     */
    @Override
    public EventFullDto authorUpdateEvent(Long uId, Long eId, EventUpdateByUserRequestDto userRequestToUpdateEvent) {
        UserRequestModerationState action = null;
        var actionField = userRequestToUpdateEvent.stateAction();
        if (actionField != null) {
            try {
                action = UserRequestModerationState.valueOf(actionField);
            } catch (IllegalArgumentException exception) {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((INVALID_DATA_SET)),
                        "Тип операции для обработки анкеты события отсутствует или не распознан"
                );
            }
        }
        var event = getUserEvent(uId, eId);
        var eventState = event.getState();
        if (CANCEL_REVIEW.equals(action)) {
            if (eventState.equals(EventState.PENDING)) {
                event.setState(CANCELED);
                return eventRepository.save(event).toEventFullDto();
            } else {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((CONFLICT)),
                        "Афиша события уже опубликована, или модератор уже вернул ее на доработку");
            }
        } else if (SEND_TO_REVIEW.equals(action)) {
            if (eventState.equals(CANCELED)) {
                event.setState(EventState.PENDING);
            } else {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((CONFLICT)),
                        "Нельзя отправить на ревью уже опубликованную афишу события, или уже проходящую ревью");
            }
        } else if (action == null && eventState.equals(PUBLISHED)) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((CONFLICT)),
                    "Нельзя редактировать уже опубликованную афишу события");
        }
        Instant plannedDateTime;
        String dateTime = userRequestToUpdateEvent.eventDate();
        if (dateTime != null) {
            plannedDateTime = toInstantTime(dateTime);
            var now = Instant.now(Clock.systemUTC());
            if (plannedDateTime.isBefore(now.plus(AUTHORS_ACTION_MODERATION_LIMIT, HOURS))) {
                    throw new EwmAppRequestValidateException(
                            thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((INVALID_DATA_SET)),
                            "Указана неверная дата, она должна быть в будущем, " +
                            " и до ее начала должно быть не менее 2 часов.");
            }
        } else {
            plannedDateTime = null;
        }
        Optional.ofNullable(userRequestToUpdateEvent.category()).ifPresent(categoryId ->
                event.setCategoryEntity(getCategory(Long.valueOf(categoryId))));
        Optional.ofNullable(userRequestToUpdateEvent.annotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(userRequestToUpdateEvent.description()).ifPresent(event::setDescription);
        Optional.ofNullable(dateTime).ifPresent(eventStart -> event.setEventDate(plannedDateTime));
        Optional.ofNullable(userRequestToUpdateEvent.location())
                .ifPresent(eventLocation -> event.setLocation(createLocation(eventLocation)));
        Optional.ofNullable(userRequestToUpdateEvent.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(userRequestToUpdateEvent.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(userRequestToUpdateEvent.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(userRequestToUpdateEvent.title()).ifPresent(event::setTitle);
        return eventRepository.save(event).toEventFullDto();
    }

    /**
     * Создание заявки не регистрацию в событие/мероприятие. Сценарии валидации запроса. Постановка его в очередь
     * на рассмотрение. Авто регистрация, если доступна.
     * @param uId идентификатор кандидата
     * @param eId желаемое событие
     * @return состояние заявки
     */
    @Override
    public ParticipationRequestDto createParticipationRequest(Long uId, Long eId) {
        var user = getUser(uId);
        var isUserEvent = user.getUserEventEntities().stream().map(EventEntity::getId).anyMatch(e -> e.equals(eId));
        if (isUserEvent) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Пользователь не может подать заявку на свое же мероприятие"
            );
        }
        var event = getEvent(eId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Мероприятие недоступно для регистрации, подождите его публикации"
            );
        }
        var requestStatus = createParticipationRequestState(event);
        var requestToEvent = new ParticipationRequestEntity();
        requestToEvent.setRequester(user);
        requestToEvent.setStatus(requestStatus);
        requestToEvent.setEvent(event);
        requestToEvent.setCreated(Instant.now(Clock.systemUTC()));
        var response = participationRequestRepository.save(requestToEvent).toDto();
        updateConfirmedMembersQuantityForEvent(eId);
        return response;
    }

    private ParticipationRequestState createParticipationRequestState(EventEntity event) {
        var allAddedMembers = event.getConfirmedRequests();
        var limit = event.getParticipantLimit();
        var isModerationOn = event.getRequestModeration();
        if (isModerationOn) {
            if (limit == 0) {
                return CONFIRMED;
            } else {
                return ParticipationRequestState.PENDING;
            }
        } else {
            if (limit == 0 || (allAddedMembers < limit)) {
                return ParticipationRequestState.CONFIRMED;
            } else {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "На мероприятие нет свободных мест"
                );
            }
        }
    }

    /**
     * Обработка запроса модератора на действия с поставленной в запрос на публикацию анкетой события.
     * Публикация, возврат на редактирование, корректировка данных. Сценарии проверок на правила модерации.
     * @param eId идентификатор нужной анкеты
     * @param adminActionForEvent запрос модератора
     * @return актуальное состояние запроса после его обработки
     */
    @Transactional
    @Override
    public EventFullDto adminUpdateEvent(Long eId, EventUpdateByAdminRequestDto adminActionForEvent) {
        AdminRequestModerationEventState adminRequest = null;
        var action = adminActionForEvent.stateAction();
        if (action != null) {
            try {
                adminRequest = AdminRequestModerationEventState.valueOf(adminActionForEvent.stateAction());
            } catch (IllegalArgumentException exception) {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((INVALID_DATA_SET)),
                        "Запрос на обновление статуса обработки анкеты события не распознан"
                );
            }
        }
        var event = getEvent(eId);
        var now = Instant.now(Clock.systemUTC());
        if (PUBLISH_EVENT.equals(adminRequest)) {
            checkActionTimeLimitBeforePublication(
                    now,
                    event.getEventDate(),
                    ADMINS_ACTION_MODERATION_LIMIT);
            if (event.getState().equals(CANCELED)) {
            throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно опубликовать афишу события - автор не запрашивал ее публикацию"
                );
            } else if (event.getState().equals(PUBLISHED)) {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно опубликовать афишу события - она уже опубликована"
                );
            }
        } else if (REJECT_EVENT.equals(adminRequest)) {
            if (event.getState().equals(EventState.PENDING)) {
                event.setState(CANCELED);
                return eventRepository.save(event).toEventFullDto();
            } else if (event.getState().equals(PUBLISHED)) {
                throw new EwmAppConflictActionException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно отменить редактирование афиши события - она опубликована," +
                                " и не было запросов на ее редактирование"
                );
            }
        } else if (adminRequest != null) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Данная операция <%s> для администратора недоступна, или еще не реализована"
            );
        }
        Optional.ofNullable(adminActionForEvent.annotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(adminActionForEvent.description()).ifPresent(event::setDescription);
        Optional.ofNullable(adminActionForEvent.category()).ifPresent(category ->
                event.setCategoryEntity(getCategory(Long.valueOf(category))));
        var stringDatetime = adminActionForEvent.eventDate();
        if (stringDatetime != null && toInstantTime(stringDatetime)
                .isBefore(Instant.now(Clock.systemUTC()).plus(2, HOURS))
        ) {
            throw new EwmAppRequestValidateException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Предполагаемая дата начала события должна быть не ранее 2 часов от момента публикации"
            );
        }
        Optional.ofNullable(adminActionForEvent.eventDate()).ifPresent(this::toInstantTime);
        Optional.ofNullable(adminActionForEvent.location()).ifPresent(location ->
                event.setLocation(createLocation(location)));
        Optional.ofNullable(adminActionForEvent.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(adminActionForEvent.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(adminActionForEvent.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(adminActionForEvent.title()).ifPresent(event::setTitle);
        event.setState(PUBLISHED);
        event.setPublishedOn(Instant.now(Clock.systemUTC()));
        return eventRepository.save(event).toEventFullDto();
    }

    /**
     * Логика обработки запроса автора афиши события имеющихся заявок на участие в мероприятии от пользователей
     * @param eventRequests запрос с заявками, которые нужно обработать
     * @param uId идентификатор автора афиши
     * @param eId идентификатор его события
     * @return результат обработки запроса
     */
    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(
            EventRequestStatusUpdateRequest eventRequests, Long uId, Long eId) {
            var event = getUserEvent(uId, eId);
            var limit = event.getParticipantLimit();
            var confirmedRequests = new ArrayList<ParticipationRequestDto>();
            var rejectedRequests = new ArrayList<ParticipationRequestDto>();
            var requestsIsModerated = event.getRequestModeration();
            if (eventRequests.status().equals(CONFIRMED.name())) {
                if (requestsIsModerated || (limit != 0)) {
                    var quote = limit - event.getConfirmedRequests();
                    if (quote <= 0) {
                        throw new EwmAppConflictActionException(
                                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                                "На мероприятии больше нет свободных мест"
                        );
                    }
                    var requests = participationRequestRepository
                            .getAllFromRequestTargetList(eId, eventRequests.requestIds());
                    for (var request : requests) {
                        if (request.getStatus().equals(ParticipationRequestState.PENDING)) {
                            if (quote == 0) {
                                request.setStatus(REJECTED);
                                rejectedRequests.add(request.toDto());
                            } else {
                                request.setStatus(CONFIRMED);
                                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                                confirmedRequests.add(request.toDto());
                                quote--;
                            }
                        }
                    }
                    participationRequestRepository.saveAll(requests);
                    updateConfirmedMembersQuantityForEvent(eId);
                }
            } else if (eventRequests.status().equals(REJECTED.name())) {
                var requests = participationRequestRepository
                        .getAllFromRequestTargetList(eId, eventRequests.requestIds());
                for (var request : requests) {
                    if (request.getStatus().equals(CONFIRMED)) {
                        throw new EwmAppConflictActionException(
                                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                                "Одна из перечисленных заявок уже одобрена организатором мероприятия, " +
                                        "повторите запрос с корректным списком заявок"
                        );
                    }
                    request.setStatus(REJECTED);
                    rejectedRequests.add(request.toDto());
                }
                participationRequestRepository.saveAll(requests);
                updateConfirmedMembersQuantityForEvent(eId);
            } else {
                throw new EwmAppRequestValidateException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                        "Некорректный статус :".concat(eventRequests.status())
                );
            }
            return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private void updateConfirmedMembersQuantityForEvent(Long eId) {
        var sum = participationRequestRepository.countByEvent_IdAndStatus(eId, CONFIRMED);
        eventRepository.updateConfirmedRequestsById(sum, eId);
    }

    /**
     * Отмена пользователем своей заявки на участие в мероприятии
     * @param rId идентификатор заявки
     * @param uId идентификатор ее автора
     * @return новый статус заявки
     */
    @Override
    public ParticipationRequestDto cancelRequest(Long rId, Long uId) {
        getUser(uId);
        var request = participationRequestRepository.findByIdAndRequesterId(rId, uId)
                .orElseThrow(() -> new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        REQUEST_NOT_FOUND.concat(rId.toString())
                        )
                );
        if (request.getStatus().equals(CONFIRMED)) {
            request.setStatus(ParticipationRequestState.CANCELED);
            request.getEvent().setConfirmedRequests(request.getEvent().getConfirmedRequests() - 1);
        } else if (request.getStatus().equals(ParticipationRequestState.PENDING)) {
            request.setStatus(ParticipationRequestState.CANCELED);
        } else if (request.getStatus().equals(ParticipationRequestState.CANCELED)) {
            participationRequestRepository.delete(request);
            return new ParticipationRequestDto(
                    null,
                    null,
                    "DELETED",
                    null,
                    toLocalDateTime(Instant.now(Clock.systemUTC())));
        }
        return request.toDto();
    }

    /**
     * Обработка запроса автора анкеты события на просмотр всех заявок на его событие
     * @param eId идентификатор его афиши
     * @param uId идентификатор самого автора
     * @return запрошенный список заявок
     */
    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long eId, Long uId) {
        getUserEvent(uId, eId);
        return participationRequestRepository.findAllByEventIdOrderByRequesterId(eId)
                .stream()
                .map(ParticipationRequestEntity::toDto)
                .toList();
    }

    /**
     * Обработка запроса пользователя на получение всех его заявок на возможные мероприятия
     * @param uId идентификатор пользователя
     * @return список его заявок
     */
    @Override
    public List<ParticipationRequestDto> getAllUserRequests(Long uId) {
        return participationRequestRepository.findAllByRequesterIdOrderById(uId)
                .stream()
                .map(ParticipationRequestEntity::toDto)
                .toList();
    }

    /**
     * Получить полную информацию о событии
     * @param eId его идентификатор
     * @return событие
     */
    @Override
    public EventFullDto getFullEvent(Long eId) {
        var event = getEvent(eId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new EwmAppEntityNotFoundException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                    String.format("Не найдено опубликованное событие ID %d", eId)
            );
        }
        return eventRepository.save(event).toEventFullDto();
    }

    /**
     * Получить список доступных событий по набору критериев
     * @param params критерии для поиска
     * @return список анкет
     */
    @Override
    public List<EventShortDto> getEventsByCriteria(Map<String, String> params) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventEntity> query = builder.createQuery(EventEntity.class);
        Root<EventEntity> event = query.from(EventEntity.class);
        query.select(event);

        Order order = null;
        var sortOrder = params.get("sort");
        if (sortOrder != null) {
            if ("EVENT_DATE".equalsIgnoreCase(params.get("sort"))) {
                order = builder.asc(event.get("eventDate"));
            } else if ("VIEWS".equalsIgnoreCase(params.get("sort"))) {
                order = builder.desc(event.get("views"));
            } else {
                throw new EwmAppRequestValidateException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                        "Этот режим сортировки не реализован"
                );
            }
        }
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(event.get("state"), PUBLISHED));
        var text = params.get("text");
        if (text != null) {
            var lowerAnnotation = builder.lower(event.get("annotation").as(String.class));
            var lowerDescription =  builder.lower(event.get("description").as(String.class));
            var textSearchCriteria = builder.or(
                    builder.like(lowerAnnotation, '%' + text.toLowerCase(Locale.ROOT) + '%'),
                    builder.like(lowerDescription, '%' + text.toLowerCase(Locale.ROOT) + '%')
            );
            predicates.add(textSearchCriteria);
        }
        var categoriesStr  = params.get("categories");
        if (categoriesStr != null) {
            var categoriesInt = parseStringToListInt(categoriesStr);
            var entityCategories = builder.in(event.get("categoryEntity"));
            for (var categoryInt : categoriesInt) {
                entityCategories.value(getCategory(categoryInt));
            }
            predicates.add(entityCategories);
        }
        var paid = params.get("paid");
        if (paid != null) {
            predicates.add(builder.equal(event.get("paid"), Boolean.valueOf(paid)));
        }
        var start = (params.get("rangeStart") == null)
                ? Instant.now(Clock.systemUTC()) : toInstantTime(params.get("rangeStart"));
        var end = (params.get("rangeEnd") == null)
                ? Instant.now(Clock.systemUTC()).plus(1000, DAYS) : toInstantTime(params.get("rangeEnd"));
        if (end.isBefore(start) || end.equals(start)) {
            throw new EwmAppRequestValidateException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Дата окончания мероприятия должна быть позже ее начала"
            );
        }
        predicates.add(builder.between(event.get("eventDate"), start, end));
        var onlyAvailable = params.get("onlyAvailable");
        Predicate eventFree = builder.equal(event.get("participantLimit"), 0);
        Predicate eventHaveLimitParticipant = builder.greaterThan(event.get("participantLimit"), 0);
        if (onlyAvailable != null && onlyAvailable.equalsIgnoreCase("true")) {
            Predicate addingMembersIsStillAvailable =
                    builder.or(
                            builder.and(
                                    eventHaveLimitParticipant,
                                    builder.greaterThan(
                                            event.get("participantLimit"),
                                            event.get("confirmedRequests")
                                    )
                            ),
                            eventFree);
            predicates.add(addingMembersIsStillAvailable);
        } else {
            if (onlyAvailable != null && !onlyAvailable.equalsIgnoreCase("false")) {
                throw new EwmAppRequestValidateException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                        "Недопустимый параметр. Допускается только true или false"
                );
            }
        }
        query.where(predicates.toArray(new Predicate[0]));
        if (order != null) {
            query.orderBy(order);
        }
        var from = (params.get("from") == null) ? 0 : Integer.parseInt(params.get("from"));
        var size = (params.get("size") == null) ? 10 : Integer.parseInt(params.get("size"));
        List<EventEntity> resultList = entityManager
                .createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        return resultList.stream().map(EventEntity::toEventShortDto).toList();
    }

    /**
     * Получение списка событий
     * @param params параметры запроса
     * @return список событий
     */
    @Override
    public List<EventFullDto> findAllStats(Map<String, String> params) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventEntity> query = builder.createQuery(EventEntity.class);
        Root<EventEntity> event = query.from(EventEntity.class);
        query.select(event);

        builder.asc(event.get("id"));
        List<Predicate> predicates = new ArrayList<>();
        var userStrIds  = params.get("users");
        if (userStrIds != null) {
            var userIds = parseStringToListInt(userStrIds);
            var predicateUserList = builder.in(event.get("initiator"));
            for (var initiatorId : userIds) {
                predicateUserList.value(getUser(initiatorId));
            }
            predicates.add(predicateUserList);
        }
        var eventStatesStr  = params.get("states");
        if (eventStatesStr != null) {
            var eventStates = parseStringToListEventState(eventStatesStr);
            var predicateEventStatesList = builder.in(event.get("state"));
            for (var eventState : eventStates) {
                predicateEventStatesList.value(eventState);
            }
            predicates.add(predicateEventStatesList);
        }
        var categoriesStr  = params.get("categories");
        if (categoriesStr != null) {
            var categoriesInt = parseStringToListInt(categoriesStr);
            var entityCategories = builder.in(event.get("categoryEntity"));
            for (var categoryInt : categoriesInt) {
                entityCategories.value(getCategory(categoryInt));
            }
            predicates.add(entityCategories);
        }
        var start = (params.get("rangeStart") == null)
                ? Instant.now(Clock.systemUTC()).minus(1000, DAYS)
                : toInstantTime(params.get("rangeStart"));
        var end = (params.get("rangeEnd") == null)
                ? Instant.now(Clock.systemUTC()).plus(1000, DAYS)
                : toInstantTime(params.get("rangeEnd"));
        if (end.isBefore(start) || end.equals(start)) {
            throw new EwmAppRequestValidateException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Дата окончания мероприятия должна быть позже ее начала"
            );
        }
        predicates.add(builder.between(event.get("eventDate"), start, end));
        var from = (params.get("from") == null) ? 0 : Integer.parseInt(params.get("from"));
        var size = (params.get("size") == null) ? 10 : Integer.parseInt(params.get("size"));
        query.where(predicates.toArray(new Predicate[0]));
        List<EventEntity> resultList = entityManager
                .createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        return resultList.stream().map(EventEntity::toEventFullDto).toList();
    }

    /**
     * Обновление количества уникальных просмотров анкеты
     * @param eId идентификатор анкеты события
     */
    @Override
    public void addView(Long eId) {
        eventRepository.updateViewsById(eId);
    }

    private List<Long> parseStringToListEventState(String params) {
        List<Long> result = new ArrayList<>();
        String[] eventStates = params.split(",");
        try {
            for (String eventState : eventStates) {
                result.add((long) valueOf(eventState).ordinal());
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw new EwmAppRequestValidateException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Недопустимый параметр."
            );
        }
    }

    private List<Long> parseStringToListInt(String params) {
        List<Long> result = new ArrayList<>();
        String[] elements = params.split(",");
        for (String element : elements) {
            var intValue = Long.parseLong(element);
            if (intValue <= 0) {
                throw new EwmAppRequestValidateException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                        "Значение должно быть больше 0"
                );
            }
            result.add(intValue);
        }
        return result;
    }

    private CategoryEntity getCategory(Long cId) {
        return categoryRepository
                .findById(cId)
                .orElseThrow(() -> new EwmAppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(cId)))
                );
    }

    private UserEntity getUser(Long uId) {
        return userRepository.findById(uId).orElseThrow(() ->
                new EwmAppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        "В репозитории не найден пользователь с ID: ".concat(String.valueOf(uId))
                ));
    }

    private EventEntity getEvent(Long eId) {
        return eventRepository.findById(eId).orElseThrow(() ->
                new EwmAppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        String.format("В репозитории не найдено событие ID %d", eId)
                )
        );
    }

    private EventLocationEntity createLocation(LocationDto location) {
        var entityLocation = new EventLocationEntity();
        entityLocation.setLat(location.lat());
        entityLocation.setLon(location.lon());
        return eventLocationRepository.save(entityLocation);
    }

    private Instant toInstantTime(String stringDatetime) {
        return Instant.from(LocalDateTime
                .parse(stringDatetime, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                .atZone(ZoneId.of(GMT)));
    }

    private String toLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.of(GMT)).format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    private EventEntity getUserEvent(Long uId, Long eId) {
        return getUser(uId).getUserEventEntities().stream()
                .filter(eventEntity -> eventEntity.getId().equals(eId))
                .findFirst()
                .orElseThrow(() -> new EwmAppEntityNotFoundException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        String.format("Пользователь не создавал афишу событию с ID %d", eId)));
    }

    private void checkActionTimeLimitBeforePublication(
            Instant actionDateTime,
            Instant publicationDateTime,
            int limitBeforePublication) {
        if (actionDateTime.isAfter(publicationDateTime.minus(limitBeforePublication, HOURS))) {
            throw new EwmAppRequestValidateException(
                    thisService,
                    REQUEST_NOT_COMPLETE.concat(SPLITTER)
                            .concat(CONFLICT),
                    String.format("Указанное действие с афишей события должно быть выполнено не ранее, чем за "
                            .concat("%d час(а) до начала самого события"), limitBeforePublication));
        }
    }

    private void checkCategoryById(Long cId) {
        if (!categoryRepository.existsById(cId)) {
            throw new EwmAppEntityNotFoundException(
                    thisService,
                    REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                    CATEGORY_NOT_FOUND.concat(String.valueOf(cId))
            );
        }
    }

    /**
     * Добавление модератором подборки событий
     * @param dto макет подборки
     * @return опубликованная подборка
     */
    @Override
    public CompilationDto addCompilation(CompilationNewDto dto) {
        return compilationRepository.save(dto.toEntity(eventRepository, compilationEventRelationRepository)).toDto();
    }

    /**
     * Получение пользователем любой подборки событий по ее идентификатору
     * @param cpId идентификатор подборки
     * @return найденная подборка
     */
    @Override
    public Optional<CompilationDto> getCompilationById(Long cpId) {
        return compilationRepository.findById(cpId).map(CompilationEntity::toDto);
    }

    /**
     * Найти подборку событий по набору критериев
     * @param pinned прикреплена к главной странице или нет
     * @param from искать с номера позиции в списке имеющихся
     * @param size размер выборки
     * @return полученный список
     */
    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        var result = compilationRepository.findAllPinned(pinned, size, from);
        return result.stream().map(CompilationEntity::toDto).toList();
    }

    /**
     * Удалить подборку
     * @param cpId идентификатор подборки
     */
    @Override
    public void deleteCompilation(Long cpId) {
        if (!compilationRepository.existsById(cpId)) throw new EwmAppEntityNotFoundException(
                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                "Подборка не найдена"
        );
        compilationRepository.deleteById(cpId);
    }

    /**
     * Обновить содержимое подборки модератором
     * @param cpId идентификатор подборки
     * @param dto данные для обновления
     * @return обновленная подборка
     */
    @Override
    public CompilationDto updateCompilation(Long cpId, CompilationUpdateRequestDto dto) {
        CompilationEntity compilation = compilationRepository.findById(cpId)
                .orElseThrow(() ->
                        new EwmAppEntityNotFoundException(
                                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                                "Подборка не найдена")
                );
        compilation.getEvents().clear();
        Set<Long> eventsIds = (dto.events() == null) ? new HashSet<>() : dto.events();
        compilation.setEvents(updateCompilationIEvents(
                eventsIds, compilation, eventRepository, compilationEventRelationRepository));
        compilation.setPinned((dto.pinned() == null) ? compilation.getPinned() : dto.pinned());
        compilation.setTitle((dto.title() == null) ? compilation.getTitle() : dto.title());
        return compilationRepository.save(compilation).toDto();
    }

    private Set<CompilationEventRelation> updateCompilationIEvents(
            Set<Long> eventsIds,
            CompilationEntity compilationEntity,
            EventRepository eventRepository,
            CompilationEventRelationRepository relationRepository) {
        LinkedHashSet<EventEntity> events = new LinkedHashSet<>(eventRepository.findAllById(eventsIds));
        var relations = new LinkedHashSet<CompilationEventRelation>();
        for (var event : events) {
            var relation = new CompilationEventRelation();
            relation.setCompilation(compilationEntity);
            relation.setEvent(event);
            relations.add(relationRepository.save(relation));
        }
        return relations;
    }

    /**
     * Поставить анкете рейтинг "событие заинтересовало"
     * @param uId пользователь
     * @param eId анкета
     */
    @Override
    @Transactional
    public void addEventExpectationRating(Long uId, Long eId) {
        var event = getEvent(eId);
        if (event.getEventDate().isBefore(Instant.now(Clock.systemUTC()))
        || !event.getState().equals(PUBLISHED)) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Оценить можно только предстоящее и опубликованное событие"
            );
        }
        var rate = new EventExpectationRatingEntity();
        rate.setEvent(event);
        rate.setUser(getUser(uId));
        try {
            ratingExpectationRepository.save(rate);
        } catch (DataIntegrityViolationException exception) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Пользователь уже отметил событие как 'интересное'"
            );
        }
        var eventStats = ratingStatRepository.findById(eId).get();
        eventStats.setExpectationRate(eventStats.getExpectationRate() + 1);
        ratingStatRepository.save(eventStats);
    }

    /**
     * Получить информацию об рейтингах анкеты
     * @param eId идентификатор анкеты события
     * @return анкета
     */
    @Override
    public EventRateDto getEventRating(Long eId) {
        var event = getEvent(eId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Посмотреть рейтинг можно только у опубликованного события"
            );
        }
        var expectation = ratingExpectationRepository.countByEvent_Id(eId);
        return new EventRateDto(
                event.getId(),
                event.getAnnotation(),
                expectation,
                getEventSatisfactionRating(event).toString(),
                event.getCategoryEntity().getName(),
                event.getInitiator().getName(),
                0L,
                event.getViews(),
                event.getConfirmedRequests(),
                toLocalDateTime(event.getEventDate()),
                toLocalDateTime(event.getCreatedOn()),
                event.getPaid(),
                event.getLocation().toDto(),
                event.getDescription()
        );
    }

    /**
     * Получить "Топ" по рейтингам
     * @param top
     * @return
     */
    @Override
    public List<EventsStatsDto> getRatings(Integer top) {
        var result = ratingStatRepository.findExpectationsTop(
                Instant.now(Clock.systemUTC()),
                PUBLISHED,
                top
        );
        var topList = new ArrayList<EventsStatsDto>();
        EventEntity event;
        for (var element : result) {
            event = element.getEvent();
            topList.add(new EventsStatsDto(
                    event.getId(),
                    element.getExpectationRate(),
                    getEventSatisfactionRating(event).toString(),
                    event.getViews(),
                    event.getTitle(),
                    event.getCategoryEntity().getName(),
                    event.getInitiator().getName(),
                    toLocalDateTime(event.getEventDate()),
                    toLocalDateTime(event.getPublishedOn())
            ));
        }
        return topList;
    }

    /**
     * Поставить событию после участия в нем оценку от 1 до 10
     * @param uId пользователь
     * @param eId событие
     * @param ratingStr оценка
     */
    @Override
    @Transactional
    public void addEventSatisfactionRating(Long uId, Long eId, String ratingStr) {
        var event = getEvent(eId);
        var user = getUser(uId);
        int rate;
        try {
            rate = Integer.parseInt(ratingStr);
            if (rate < 1 || rate > 10) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            throw new EwmAppRequestValidateException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Оценка не распознана - укажите значение от 1 до 10 включительно"
            );
        }
        var userIsNotMemberOfEvent = !participationRequestRepository
                .existsByEvent_IdAndRequester_IdAndStatus(
                        eId, uId, CONFIRMED
                );
        if (userIsNotMemberOfEvent || event.getEventDate().isAfter(Instant.now(Clock.systemUTC()))
                || !event.getState().equals(PUBLISHED)) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Оценить можно только опубликованное начавшееся/закончившееся событие, и только" +
                            " если пользователь в нем участвует/участвовал"
            );
        }
        var rating = new EventSatisfactionRatingEntity();
        rating.setEvent(event);
        rating.setUser(user);
        rating.setSatisfactionRating(rate);
        try {
            ratingSatisfactionRepository.save(rating);
        } catch (DataIntegrityViolationException exception) {
            throw new EwmAppConflictActionException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Пользователь уже поставил оценку событию"
            );
        }
        var eventStats = ratingStatRepository.findById(eId).get();
        var summarySatisfactionRate = eventStats.getSummarySatisfactionRate();
        summarySatisfactionRate = summarySatisfactionRate + rate;
        eventStats.setSummarySatisfactionRate(summarySatisfactionRate);
        ratingStatRepository.save(eventStats);
    }

    private String getEventSatisfaction(EventEntity event) {
        var eId = event.getId();
        String satisfaction;
        if (event.getEventDate().isAfter(Instant.now(Clock.systemUTC()))) {
            satisfaction = "Составление рейтинга станет доступно после начала события";
        } else {
            var summarySatisfaction = ratingSatisfactionRepository.findByEvent_Id(eId);
            var sum = summarySatisfaction
                    .stream()
                    .mapToInt(EventSatisfactionRatingEntity::getSatisfactionRating)
                    .sum();
            satisfaction = String.valueOf(sum / summarySatisfaction.size());
        }
        return satisfaction;
    }

    private Long getEventSatisfactionRating(EventEntity event) {
        var eId = event.getId();
        long satisfactionRating;
        var membersCount = ratingSatisfactionRepository.countByEvent_Id(eId);
        if (membersCount == 0) {
            satisfactionRating = 0L;
        } else {
            var summarySatisfaction = ratingStatRepository.findByEvent_Id(eId).get().getSummarySatisfactionRate();
            satisfactionRating = summarySatisfaction / membersCount;
        }
        return satisfactionRating;
    }
}
