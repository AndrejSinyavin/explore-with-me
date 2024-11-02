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
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.exception.AppRequestValidateException;
import ru.practicum.ewm.ewmservice.service.EwmService;
import ru.practicum.ewm.ewmservice.service.ApiUser.SearchCriteria;

import java.util.List;

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
    static final String SIZE = "size";
    static final String FROM = "from";
    static Integer FROM_DEFAULT = 0;
    static Integer SIZE_DEFAULT = 10;
    static String POST_USER_REQUEST = "\n==>   Запрос POST: создать пользователя {}";
    static String POST_CATEGORY_REQUEST = "\n==>   Запрос POST: создать категорию {}";
    static String DELETE_USER_REQUEST = "\n==>   Запрос DELETE: удалить пользователя ID {}";
    static String GET_USERS_REQUEST =
            "\n==>   Запрос GET: получить список пользователей из диапазона: pageFrom {} pageSize {} ids {} ";
    static String PATCH_CATEGORY_REQUEST = "\n==>   Запрос PATCH: обновить категорию ID {}: {}";
    static String CREATED_USER_RESPONSE = "\n<==   Ответ: '201 Created' Запрос выполнен - создан пользователь {}";
    static String CREATED_CATEGORY_RESPONSE = "\n<==   Ответ: '201 Created' Запрос выполнен - создана категория {}";
    static String DELETED_USER_RESPONSE = "\n<==   Ответ: '204 No Content' Запрос выполнен - пользователь ID {} удален";
    static String GET_USERS_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - список запрошенных пользователей: {}";
    static String PATCH_CATEGORY_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - обновленная категория: {}";
    static String DELETE_CATEGORY_REQUEST = "\n==>   Запрос DELETE: удалить категорию {}";
    static String DELETED_CATEGORY_RESPONSE = "\n<==   Ответ: '204 No Content' Запрос выполнен - категория ID {} удалена";
    static String VALIDATION_ERROR = "Ошибка при валидации API запроса";
    static String INVALID_SEARCH_CRITERIA = "Допускается запрашивать либо список, либо диапазон ID пользователей";
    String thisService = this.getClass().getName();

    EwmService ewmService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto addUser(@Valid @RequestBody UserNewDto request) {
        log.info(POST_USER_REQUEST, request.toString());
        var response = ewmService.addUser(request);
        log.info(CREATED_USER_RESPONSE, response);
        return response;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{user-id}")
    public void deleteUser(@Positive(message = POSITIVE) @PathVariable(value = UID) Long uid) {
        log.info(DELETE_USER_REQUEST, uid);
        ewmService.deleteUser(uid);
        log.info(DELETED_USER_RESPONSE, uid);
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
            throw new AppRequestValidateException(thisService, VALIDATION_ERROR, INVALID_SEARCH_CRITERIA);
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
        log.info(POST_CATEGORY_REQUEST, categoryNewDto.toString());
        var categoryDto = ewmService.addCategory(categoryNewDto);
        log.info(CREATED_CATEGORY_RESPONSE, categoryDto);
        return categoryDto;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories/{cat-id}")
    public void deleteCategoryById(@Positive(message = POSITIVE) @PathVariable(value = CID) Long cId) {
        log.info(DELETE_CATEGORY_REQUEST, cId);
        ewmService.deleteCategoryById(cId);
        log.info(DELETED_CATEGORY_RESPONSE, cId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/categories/{cat-id}")
    public CategoryDto patchCategoryById(
            @Valid @RequestBody CategoryNewDto categoryNewDto,
            @Positive(message = POSITIVE) @PathVariable(value = CID) Long cId
    ) {
        log.info(PATCH_CATEGORY_REQUEST, cId, categoryNewDto.toString());
        var categoryDto = ewmService.patchCategoryById(cId, categoryNewDto);
        log.info(PATCH_CATEGORY_RESPONSE, categoryDto);
        return categoryDto;
    }

}
