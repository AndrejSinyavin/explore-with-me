package ru.practicum.ewm.statsserver.server.exception;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

/**
 * Централизованный обработчик исключений сервиса статистики.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestControllerAdvice
public class AppExceptionHandlers {
    static String SEPARATOR = ". ";
    static String BAD_REQUEST = "'400 Bad Request' ";
    static String INTERNAL_SERVER_ERROR = "'500 Internal Server Error' ";
    static String NOT_READABLE_BODY = "Тело запроса некорректное или отсутствует";
    static String SERVER_ERROR = "Сервер не смог обработать запрос";
    static String SERVER_FAILURE = "Сбой в работе сервера";
    static String LOG_RESPONSE_THREE = "Ответ <= {} {} \n{}";
    static String LOG_RESPONSE_FIVE = "Ответ <= {} {} {} {} \n{}";


    /**
     * Обработчик исключений для ответов BAD_REQUEST для запросов
     * с отсутствующим или несоответствующим форматом тела или заголовков.
     *
     * @param e перехваченное исключение
     * @return стандартный API-ответ об ошибке ErrorResponse с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableExceptionResponse(final HttpMessageNotReadableException e) {
        log.warn(LOG_RESPONSE_THREE, BAD_REQUEST, NOT_READABLE_BODY, e.getStackTrace());
        return new ErrorResponse(BAD_REQUEST, NOT_READABLE_BODY);
    }

    /**
     * Обработчик исключений для ответов INTERNAL_SERVER_ERROR
     *
     * @param e перехваченное исключение
     * @return стандартный API-ответ об ошибке ErrorResponse с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({InternalServiceException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerInternalErrorResponse(final AppException e) {
        log.error(LOG_RESPONSE_FIVE, INTERNAL_SERVER_ERROR.concat(SERVER_ERROR),
                e.getSource(), e.getError(), e.getMessage(), e.getStackTrace());
        return new ErrorResponse(
                INTERNAL_SERVER_ERROR,
                SERVER_ERROR.concat(SEPARATOR).concat(e.getLocalizedMessage())
        );
    }

    /**
     * Обработчик исключений - заглушка, для обработки прочих непредусмотренных исключений.
     *
     * @param e перехваченное исключение
     * @return стандартный API-ответ об ошибке ErrorResponse с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerFailureResponse(final Throwable e) {
        log.error(INTERNAL_SERVER_ERROR.concat(SERVER_FAILURE).concat(Arrays.toString(e.getStackTrace())));
        return new ErrorResponse(INTERNAL_SERVER_ERROR.concat(SERVER_FAILURE), e.getMessage());
    }

}
