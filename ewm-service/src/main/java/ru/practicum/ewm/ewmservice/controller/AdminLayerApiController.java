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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CategoryNewDto;
import ru.practicum.ewm.ewmservice.dto.CompilationDto;
import ru.practicum.ewm.ewmservice.dto.CompilationNewDto;
import ru.practicum.ewm.ewmservice.dto.CompilationUpdateRequestDto;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByAdminRequestDto;
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.exception.EwmAppRequestValidateException;
import ru.practicum.ewm.ewmservice.service.EwmService;
import ru.practicum.ewm.ewmservice.service.UserApiService.SearchCriteria;

import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminLayerApiController {
    static final String POSITIVE = "Эта величина может быть только положительным значением";
    static final String NOT_NEGATIVE = "Эта величина не может быть отрицательным значением";
    static final String LIST_IDS = "ids";
    static final String CID = "cat-id";
    static final String UID = "user-id";
    static final String EID = "event-id";
    static final String CPID = "comp-id";
    static final String SIZE = "size";
    static final String FROM = "from";
    static Integer FROM_DEFAULT = 0;
    static Integer SIZE_DEFAULT = 10;
    static String POST_USER_REQUEST = "\n==>   Запрос POST: создать пользователя {}";
    static String CREATE_CATEGORY_REQUEST = "\n==>   Запрос POST: создать категорию события {}";
    static String DELETE_USER_REQUEST = "\n==>   Запрос DELETE: удалить пользователя ID {}";
    static String GET_USERS_REQUEST =
            "\n==>   Запрос GET: получить список пользователей из диапазона: pageFrom {} pageSize {} ids {} ";
    static String UPDATE_CATEGORY_REQUEST = "\n==>   Запрос PATCH: обновить категорию события ID {}: {}";
    static String USER_CREATED = "\n<==   Ответ: '201 Created' Запрос выполнен - создан пользователь {}";
    static String CATEGORY_CREATED = "\n<==   Ответ: '201 Created' Запрос выполнен - создана категория события {}";
    static String USER_DELETED = "\n<==   Ответ: '204 No Content' Запрос выполнен - пользователь ID {} удален";
    static String GET_USERS_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - список запрошенных пользователей: {}";
    static String UPDATE_CATEGORY_CREATED = "\n<==   Ответ: '200 Ok' Запрос выполнен - категория события обновлена: {}";
    static String DELETE_CATEGORY_REQUEST = "\n==>   Запрос DELETE: удалить категорию события {}";
    static String CATEGORY_DELETED = "\n<==   Ответ: '204 No Content' Запрос выполнен, категория события ID {} удалена";
    static String ADMIN_UPDATE_REQUEST = "\n==>   Запрос PATCH: администратор редактирует событие ID {}: {} ";
    static String ADMIN_UPDATE_CREATED = "\n<==   Ответ: '200 Ok' Запрос выполнен. Событие ID {} изменено: {} ";
    static String EVENTS_ADMIN_REQUEST = "\n==>   Запрос GET: администратор запрашивает информацию о событиях {}:";
    static String EVENTS_ADMIN_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен. Результат запроса: {}";
    static String VALIDATION_ERROR = "Ошибка при валидации API запроса";
    static String INVALID_SEARCH_CRITERIA = "Допускается запрашивать либо список, либо диапазон ID пользователей";
    String thisService = this.getClass().getName();

    EwmService ewmService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto addUser(@Valid @RequestBody UserNewDto request) {
        log.info(POST_USER_REQUEST, request.toString());
        var response = ewmService.addUser(request);
        log.info(USER_CREATED, response);
        return response;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{user-id}")
    public void deleteUser(@Positive(message = POSITIVE) @PathVariable(value = UID) Long uid) {
        log.info(DELETE_USER_REQUEST, uid);
        ewmService.deleteUser(uid);
        log.info(USER_DELETED, uid);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users")
    public List<UserDto> getUsers(
            @RequestParam(value = LIST_IDS, required = false) Integer[] ids,
            @PositiveOrZero(message = NOT_NEGATIVE) @RequestParam(value = FROM, required = false) Integer pageFrom,
            @Positive(message = POSITIVE) @RequestParam(value = SIZE, required = false) Integer pageSize
    ) {
        log.info(GET_USERS_REQUEST, pageFrom, pageSize, ids);
        if ((ids != null && ids.length > 0) && (pageFrom != null || pageSize != null)) {
            throw new EwmAppRequestValidateException(thisService, VALIDATION_ERROR, INVALID_SEARCH_CRITERIA);
        } else if (ids == null || ids.length == 0) {
                if (pageFrom == null) {
                    pageFrom = FROM_DEFAULT;
                }
                if (pageSize == null) {
                    pageSize = SIZE_DEFAULT;
                }
        }
        var response = ewmService.getUsers(new SearchCriteria(ids, pageFrom, pageSize));
        log.info(GET_USERS_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/categories")
    public CategoryDto addCategory(@Valid @RequestBody CategoryNewDto categoryNewDto) {
        log.info(CREATE_CATEGORY_REQUEST, categoryNewDto.toString());
        var categoryDto = ewmService.addCategory(categoryNewDto);
        log.info(CATEGORY_CREATED, categoryDto);
        return categoryDto;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories/{cat-id}")
    public void deleteCategory(@Positive(message = POSITIVE) @PathVariable(value = CID) Long cId) {
        log.info(DELETE_CATEGORY_REQUEST, cId);
        ewmService.deleteCategoryById(cId);
        log.info(CATEGORY_DELETED, cId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/categories/{cat-id}")
    public CategoryDto updateCategory(
            @Valid @RequestBody CategoryNewDto categoryNewDto,
            @Positive(message = POSITIVE) @PathVariable(value = CID) Long cId
    ) {
        log.info(UPDATE_CATEGORY_REQUEST, cId, categoryNewDto.toString());
        var categoryDto = ewmService.updateCategoryById(cId, categoryNewDto);
        log.info(UPDATE_CATEGORY_CREATED, categoryDto);
        return categoryDto;
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/events/{event-id}")
    public EventFullDto updateEvent(
            @Positive(message = POSITIVE) @PathVariable(value = EID) Long eId,
            @Valid @RequestBody EventUpdateByAdminRequestDto updateDto
    ) {
        log.info(ADMIN_UPDATE_REQUEST, eId, updateDto);
        var response = ewmService.adminUpdateEvent(eId, updateDto);
        log.info(ADMIN_UPDATE_CREATED, eId, response);
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/events")
    public List<EventFullDto> findEvents(@RequestParam Map<String, String> params) {
        log.info(EVENTS_ADMIN_REQUEST, params);
        var response = ewmService.findAllStats(params);
        log.info(EVENTS_ADMIN_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/compilations")
    public CompilationDto addCompilation(@Valid @RequestBody CompilationNewDto dto) {
        return ewmService.addCompilation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/compilations/{comp-id}")
    public void deleteCompilation(@Positive(message = POSITIVE) @PathVariable(CPID) Long cpId) {
        ewmService.deleteCompilation(cpId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/compilations/{comp-id}")
    public CompilationDto updateCompilation(
            @Positive(message = POSITIVE) @PathVariable(CPID) Long cpId,
            @Valid @RequestBody CompilationUpdateRequestDto dto) {
        return ewmService.updateCompilation(cpId, dto);
    }

}
