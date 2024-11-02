package ru.practicum.ewm.ewmservice.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.service.EwmService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicLayerApiController {
    static final String POSITIVE = "Эта величина может быть только положительным значением";
    static final String NOT_NEGATIVE = "Эта величина не может быть отрицательным значением";
    static final String CID = "cat-id";
    static final String SIZE = "size";
    static final String FROM = "from";
    static final String FROM_DEFAULT = "0";
    static final String SIZE_DEFAULT = "10";
    static String GET_CATEGORY_REQUEST = "\n==>   Запрос GET: получить категорию события по ID {} ";
    static String GET_CATEGORIES_REQUEST =
            "\n==>   Запрос GET: получить список категорий в диапазоне pageFrom {} pageSize {}";
    static String GET_CATEGORY_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - запрошенный пользователь: {}";
    static String GET_CATEGORIES_RESPONSE = "\n<==   Ответ: '200 Ok' Запрос выполнен - запрошенные категории: {}";

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

}
