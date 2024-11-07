package ru.practicum.ewm.ewmservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CategoryNewDto;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.ewmservice.dto.EventShortDto;
import ru.practicum.ewm.ewmservice.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.ewmservice.dto.UpdateEventUserRequestDto;
import ru.practicum.ewm.ewmservice.dto.LocationDto;
import ru.practicum.ewm.ewmservice.dto.ParticipationRequestDto;
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState;
import ru.practicum.ewm.ewmservice.entity.CategoryEntity;
import ru.practicum.ewm.ewmservice.entity.EventEntity;
import ru.practicum.ewm.ewmservice.entity.EventLocationEntity;
import ru.practicum.ewm.ewmservice.entity.EventState;
import ru.practicum.ewm.ewmservice.entity.UserRequestModerationState;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestEntity;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestState;
import ru.practicum.ewm.ewmservice.entity.UserEntity;
import ru.practicum.ewm.ewmservice.exception.EwmAppEntityNotFoundException;
import ru.practicum.ewm.ewmservice.exception.EwmAppRequestValidateException;
import ru.practicum.ewm.ewmservice.exception.EwmAppUnsuitableDatasetException;
import ru.practicum.ewm.ewmservice.repository.CategoryRepository;
import ru.practicum.ewm.ewmservice.repository.EventLocationRepository;
import ru.practicum.ewm.ewmservice.repository.EventRepository;
import ru.practicum.ewm.ewmservice.repository.ParticipationRequestRepository;
import ru.practicum.ewm.ewmservice.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState.PUBLISH_EVENT;
import static ru.practicum.ewm.ewmservice.entity.AdminRequestModerationEventState.REJECT_EVENT;
import static ru.practicum.ewm.ewmservice.entity.EventState.*;
import static ru.practicum.ewm.ewmservice.entity.ParticipationRequestState.CONFIRMED;
import static ru.practicum.ewm.ewmservice.entity.ParticipationRequestState.PENDING;
import static ru.practicum.ewm.ewmservice.entity.ParticipationRequestState.REJECTED;
import static ru.practicum.ewm.ewmservice.entity.UserRequestModerationState.CANCEL_REVIEW;
import static ru.practicum.ewm.ewmservice.entity.UserRequestModerationState.SEND_TO_REVIEW;

@Service
@Log4j2
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

    /**
     * @param userNewDto
     * @return
     */
    @Override
    public UserDto addUser(UserNewDto userNewDto) {
        return userRepository.save(userNewDto.toEntity()).toUserDto();
    }

    /**
     * @param criteria
     * @return
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
     * @param uId
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
     * @param categoryNewDto
     * @return
     */
    @Override
    public CategoryDto addCategory(CategoryNewDto categoryNewDto) {
        return categoryRepository.save(categoryNewDto.toEntity()).toCategoryDto();
    }

    /**
     * @param cId
     * @return
     */
    @Override
    public CategoryDto getCategoryById(Long cId) {
        return getCategory(cId).toCategoryDto();
    }

    /**
     * @param limit
     * @param offset
     * @return
     */
    @Override
    public List<CategoryDto> getCategories(int offset, int limit) {
        return categoryRepository.getPageOrderedByIdAsc(offset, limit);
    }

    /**
     * @param cId
     */
    @Override
    public void deleteCategoryById(Long cId) {
        checkCategoryById(cId);
        categoryRepository.deleteById(cId);
    }

    /**
     * @param cId
     * @param categoryNewDto
     * @return
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
     * @param uId
     * @param newEvent
     * @return
     */
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
        if (newEvent.paid() != null) createdEvent.setPaid(newEvent.paid());
        if (newEvent.participantLimit() != null) createdEvent.setParticipantLimit(newEvent.participantLimit());
        if (newEvent.requestModeration() != null) createdEvent.setRequestModeration(newEvent.requestModeration());
        createdEvent.setState(EventState.PENDING);
        return eventRepository.save(createdEvent).toEventFullDto();
    }

    /**
     * @param uId
     * @param pageFrom
     * @param pageSize
     * @return
     */
    @Override
    public List<EventFullDto> getEvents(Long uId, Integer pageFrom, Integer pageSize) {
        return eventRepository.findByInitiator_IdOrderByIdAsc(uId, pageFrom, pageSize);
    }

    /**
     * @param eId
     * @param uId
     * @return
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
     * @param uId
     * @param eId
     * @param modified
     * @return
     */
    @Override
    public EventFullDto updateEvent(Long uId, Long eId, UpdateEventUserRequestDto modified) {
        UserRequestModerationState action;
        try {
            action = UserRequestModerationState.valueOf(modified.stateAction());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((INVALID_DATA_SET)),
                    "Запрос на обновление статуса обработки анкеты события отсутствует или не опознан: " +
                            modified.stateAction()
            );
        }
        var event = getUserEvent(uId, eId);
        var eventState = event.getState();
        if (action.equals(CANCEL_REVIEW) && eventState.equals(EventState.PENDING)) {
            event.setState(CANCELED);
            return eventRepository.save(event).toEventFullDto();
        } else if (!(action.equals(SEND_TO_REVIEW) && !eventState.equals(PUBLISHED))) {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((CONFLICT)),
                    "Выполнить эту операцию невозможно по правилам модерации"
            );
        }
        var now = Instant.now(Clock.systemUTC());
        if (event.getEventDate().isBefore(now.plus(AUTHORS_ACTION_MODERATION_LIMIT, HOURS))) {
                throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((CONFLICT)),
                        "Обновить анкету можно, только если она в ожидании модерации или снята с публикации," +
                        " и до начала предполагаемого события осталось не менее 2 часов."
                );

        }
        Optional.ofNullable(modified.category()).ifPresent(categoryId ->
                event.setCategoryEntity(getCategory(Long.valueOf(categoryId))));
        Optional.ofNullable(modified.annotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(modified.description()).ifPresent(event::setDescription);
        Optional.ofNullable(modified.eventDate()).ifPresent(eventStart ->
                event.setEventDate(toInstantTime(eventStart)));
        Optional.ofNullable(modified.location()).ifPresent(eventLocation ->
                event.setLocation(createLocation(eventLocation)));
        Optional.ofNullable(modified.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(modified.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(modified.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(modified.title()).ifPresent(event::setTitle);
        event.setState(EventState.PENDING);
        return eventRepository.save(event).toEventFullDto();
    }

    /**
     * @param uId
     * @param eId
     * @return
     */
    @Override
    public ParticipationRequestDto createRequest(Long uId, Long eId) {
        var user = getUser(uId);
        var isUserEvent = user.getUserEventEntities().stream().map(EventEntity::getId).anyMatch(e -> e.equals(eId));
        if (isUserEvent) {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Пользователь не может подать заявку на свое же мероприятие"
            );
        }
        var event = getEvent(eId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                    "Мероприятие недоступно для регистрации, подождите его публикации"
            );
        }
        var requestStatus = getParticipationRequestState(event);
        var requestToEvent = new ParticipationRequestEntity();
        requestToEvent.setRequester(user);
        requestToEvent.setStatus(requestStatus);
        requestToEvent.setEvent(event);
        requestToEvent.setCreated(Instant.now(Clock.systemUTC()));
        var response = participationRequestRepository.save(requestToEvent).toDto();
        updateConfirmedMembersForEvent(eId);
        return response;
    }

    private ParticipationRequestState getParticipationRequestState(EventEntity event) {
        var preModerationForMemberIsOn = event.getRequestModeration();
        var addedMember = event.getConfirmedRequests();
        var limit = event.getParticipantLimit();
        var isHaveMemberLimit = limit != 0;
        if (preModerationForMemberIsOn) {
            if (isHaveMemberLimit && addedMember >= limit) {
                throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Мероприятие недоступно для регистрации, закончилось количество доступных мест для участников"
                );
            } else {
                return PENDING;
            }
        } else {
            if (isHaveMemberLimit && addedMember >= limit) {
                throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "На мероприятии больше нет свободных мест"
                );
            } else {
                return CONFIRMED;
            }
        }
    }

    /**
     * @param eId
     * @param adminActionForEvent
     * @return
     */
    @Override
    public EventFullDto updateEventById(Long eId, UpdateEventAdminRequestDto adminActionForEvent) {
        AdminRequestModerationEventState adminRequest;
        try {
            adminRequest = AdminRequestModerationEventState.valueOf(adminActionForEvent.stateAction());
        } catch (IllegalArgumentException exception) {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat((SPLITTER)).concat((INVALID_DATA_SET)),
                    "Неизвестный запрос на обновление статуса обработки анкеты события: "
            );
        }
        var event = getEvent(eId);
        var now = Instant.now(Clock.systemUTC());
        if (adminRequest.equals(PUBLISH_EVENT)) {
            checkActionTimeLimitBeforePublication(
                    now,
                    event.getEventDate(),
                    ADMINS_ACTION_MODERATION_LIMIT);
            if (event.getState().equals(CANCELED)) {
            throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно опубликовать афишу события - автор не запрашивал ее публикацию"
                );
            } else if (event.getState().equals(PUBLISHED)) {
                throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно опубликовать афишу события - она уже опубликована"
                );
            }
        } else if (adminRequest.equals(REJECT_EVENT)) {
            if (event.getState().equals(EventState.PENDING)) {
                event.setState(CANCELED);
                return eventRepository.save(event).toEventFullDto();
            } else if (event.getState().equals(PUBLISHED)) {
                throw new EwmAppUnsuitableDatasetException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                        "Невозможно отменить редактирование афиши события - она опубликована," +
                                " и не было запросов на ее редактирование"
                );
            }
        } else {
            throw new EwmAppUnsuitableDatasetException(
                    thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                    "Данная операция <%s> для администратора недоступна, или еще не реализована"
            );
        }
        Optional.ofNullable(adminActionForEvent.annotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(adminActionForEvent.description()).ifPresent(event::setDescription);
        Optional.ofNullable(adminActionForEvent.category()).ifPresent(category ->
                event.setCategoryEntity(getCategory(Long.valueOf(category))));
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
     * @param eventRequests
     * @param uId
     * @param eId
     * @return
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
                        throw new EwmAppUnsuitableDatasetException(
                                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                                "На мероприятии больше нет свободных мест"
                        );
                    }
                    var requests = participationRequestRepository
                            .getAllFromRequestTargetList(eId, eventRequests.requestIds());
                    for (var request : requests) {
                        if (request.getStatus().equals(PENDING)) {
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
                    updateConfirmedMembersForEvent(eId);
                }
            } else if (eventRequests.status().equals(REJECTED.name())) {
                var requests = participationRequestRepository
                        .getAllFromRequestTargetList(eId, eventRequests.requestIds());
                for (var request : requests) {
                    if (request.getStatus().equals(CONFIRMED)) {
                        throw new EwmAppUnsuitableDatasetException(
                                thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(CONFLICT),
                                "Одна из перечисленных заявок уже одобрена организатором мероприятия, " +
                                        "повторите запрос с корректным списком заявок"
                        );
                    }
                    request.setStatus(REJECTED);
                    rejectedRequests.add(request.toDto());
                }
                participationRequestRepository.saveAll(requests);
                updateConfirmedMembersForEvent(eId);
            } else {
                throw new EwmAppRequestValidateException(
                        thisService, REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(INVALID_DATA_SET),
                        "Некорректный статус :".concat(eventRequests.status())
                );
            }
            return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private void updateConfirmedMembersForEvent(Long eId) {
        var sum = participationRequestRepository.countByEvent_IdAndStatus(eId, CONFIRMED);
        eventRepository.updateConfirmedRequestsById(sum, eId);
    }

    /**
     * @param rId
     * @param uId
     * @return
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
        } else if (request.getStatus().equals(PENDING)) {
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
     * @param eId
     * @param uId
     * @return
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
     * @param uId
     * @return
     */
    @Override
    public List<ParticipationRequestDto> getAllUserRequests(Long uId) {
        return participationRequestRepository.findAllByRequesterIdOrderById(uId)
                .stream()
                .map(ParticipationRequestEntity::toDto)
                .toList();
    }

    /**
     * @param eId
     * @return
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
     * @param request
     * @param params
     * @return
     */
    @Override
    public List<EventShortDto> getEventsByCriteria(HttpServletRequest request, Map<String, String> params) {
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
     * @param request
     * @param params
     * @return
     */
    @Override
    public List<EventFullDto> findAllStats(HttpServletRequest request, Map<String, String> params) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventEntity> query = builder.createQuery(EventEntity.class);
        Root<EventEntity> event = query.from(EventEntity.class);
        query.select(event);

        Order order = builder.asc(event.get("id"));
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
     * @param eId
     */
    @Override
    public void addReview(Long eId) {
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
            throw new EwmAppUnsuitableDatasetException(
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
}
