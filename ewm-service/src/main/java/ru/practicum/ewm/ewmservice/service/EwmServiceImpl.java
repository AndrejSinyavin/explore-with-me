package ru.practicum.ewm.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CategoryNewDto;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.entity.EntityEvent;
import ru.practicum.ewm.ewmservice.entity.EntityEventLocation;
import ru.practicum.ewm.ewmservice.entity.EventState;
import ru.practicum.ewm.ewmservice.exception.AppEntityNotFoundException;
import ru.practicum.ewm.ewmservice.repository.CategoryRepository;
import ru.practicum.ewm.ewmservice.repository.EventLocationRepository;
import ru.practicum.ewm.ewmservice.repository.EventRepository;
import ru.practicum.ewm.ewmservice.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EwmServiceImpl implements EwmService {
    String thisService = this.getClass().getSimpleName();
    static String GMT = "GMT0";
    static String SPLITTER = ". ";
    static String COLON = ": ";
    static String REQUEST_COMPLETE = "Запрос выполнен";
    static String REQUEST_NOT_COMPLETE = "Запрос не выполнен";
    static String ENTITY_NOT_FOUND = "Сущность не найдена";
    static String CATEGORY_NOT_FOUND = "В репозитории не найдена категория с ID: ";
    UserRepository userRepository;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    EventLocationRepository eventLocationRepository;

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
            throw new AppEntityNotFoundException(
                    thisService,
                    REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                    "В репозитории не найден пользователь с ID: ".concat(String.valueOf(uId)));
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
        var category = categoryRepository.findById(cId);
        return category.orElseThrow(() ->
                new AppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(cId)))
                )
                .toCategoryDto();
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
        findCategoryById(cId);
        categoryRepository.deleteById(cId);
    }

    /**
     * @param cId
     * @param categoryNewDto
     * @return
     */
    @Override
    public CategoryDto patchCategoryById(Long cId, CategoryNewDto categoryNewDto) {
        categoryRepository.updateNameById(categoryNewDto.name(), cId);
        return categoryRepository.findById(cId).orElseThrow(
                () -> new AppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(cId))
                ))
                .toCategoryDto();
    }

    private void findCategoryById(Long cId) {
        if (!categoryRepository.existsById(cId)) {
            throw new AppEntityNotFoundException(
                    thisService,
                    REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                    CATEGORY_NOT_FOUND.concat(String.valueOf(cId))
            );
        }
    }

    /**
     * @param uId
     * @param eventNewDto
     * @param eventDateTime
     * @return
     */
    @Override
    @Transactional
    public EventFullDto addEvent(Long uId, EventNewDto eventNewDto, Instant eventDateTime) {
        var eventCreator = userRepository
                .findById(uId)
                .orElseThrow(() -> new AppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        "В репозитории не найден пользователь с ID: ".concat(String.valueOf(uId)))
                );
        var cId = eventNewDto.category();
        var category = categoryRepository
                .findById(cId)
                .orElseThrow(() -> new AppEntityNotFoundException(
                        thisService,
                        REQUEST_NOT_COMPLETE.concat(SPLITTER).concat(ENTITY_NOT_FOUND),
                        CATEGORY_NOT_FOUND.concat(String.valueOf(cId)))
                );
        EntityEventLocation location = new EntityEventLocation();
        location.setLat(eventNewDto.location().lat());
        location.setLon(eventNewDto.location().lon());
        location = eventLocationRepository.save(location);
        EntityEvent event = new EntityEvent();
        event.setAnnotation(eventNewDto.annotation());
        event.setEntityCategory(category);
        event.setDescription(eventNewDto.description());
        event.setEventDate(eventDateTime);
        event.setLocation(location);
        event.setTitle(eventNewDto.title());
        event.setConfirmedRequests(0L);
        event.setCreatedOn(Instant.now(Clock.systemUTC()));
        event.setInitiator(eventCreator);
        if (eventNewDto.paid() != null) event.setPaid(eventNewDto.paid());
        if (eventNewDto.participantLimit() != null) event.setParticipantLimit(eventNewDto.participantLimit());
        if (eventNewDto.requestModeration() != null) event.setRequestModeration(eventNewDto.requestModeration());
        event.setState(EventState.PENDING.name());
        var savedEvent = eventRepository.save(event);
        return savedEvent.toEventFullDto();
    }
}
