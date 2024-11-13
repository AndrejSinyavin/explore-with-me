package ru.practicum.ewm.ewmservice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Централизованный обработчик исключений сервиса приложения.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestControllerAdvice
public class EwmAppRestControllerAdvice {
    static String SPLITTER = ". ";
    static String COLON = ": ";
    static String RESPONSE = "\n<==   Ответ: ";
    static String NO_MESSAGE = "Описание ошибки не указано";
    static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String BAD_REQUEST = "400 Bad Request";
    static String INTERNAL_SERVER_ERROR = "500 Internal Server Error";
    static String CONFLICT = "409 Conflict";
    static String NOT_FOUND = "404 Not Found";
    static String NOT_READABLE_BODY = "Тело запроса некорректное или отсутствует";
    static String REQUEST_WAS_REFUSED = "Запрос отклонен";
    static String SERVER_FAILURE = "Сбой в работе сервера";
    static String INVALID_PARAMETERS = "Недопустимые параметры в запросе";
    static String ENDPOINT_ERROR = "Неверный URI, либо функционал для него на данный момент не реализован";

    /**
     * Обработчик исключений при сбое сервиса.
     *
     * @param exception исключение, вызвавшее сбой сервиса
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({EwmAppInternalServiceException.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public EwmAppErrorResponse handleInternalServerFailureResponse(final RuntimeException exception) {
        var stackTrace = exception.getStackTrace();
        var errors = new ArrayList<String>();
        if (exception.getClass().getSimpleName().equals(EwmAppInternalServiceException.class.getSimpleName())) {
            var appException = (EwmAppInternalServiceException)exception;
            errors.add(appException.getSource());
        } else {
            var name = exception.getClass().getCanonicalName();
            errors.add(name);
            errors.add("=".repeat(name.length()));
            errors.addAll(Arrays.stream(stackTrace)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
        log.error(RESPONSE.concat(INTERNAL_SERVER_ERROR).concat(SPLITTER).concat(SERVER_FAILURE)
                .concat(Arrays.toString(stackTrace)));
        return new EwmAppErrorResponse(
                INTERNAL_SERVER_ERROR,
                SERVER_FAILURE,
                exception.getLocalizedMessage(),
                timeStamp(),
                errors
        );
    }

    /**
     * Обработчик исключений для валидации заданных ограничений параметров для API запросов.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EwmAppErrorResponse handleValidationErrorResponse(final ConstraintViolationException exception) {
        var stackTrace = exception.getStackTrace();
        log.warn(RESPONSE.concat(BAD_REQUEST).concat(SPLITTER).concat(INVALID_PARAMETERS)
                .concat(Arrays.toString(stackTrace)));
        return new EwmAppErrorResponse(
                BAD_REQUEST,
                INVALID_PARAMETERS,
                exception.getLocalizedMessage(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений для валидации переданных из API параметров запросов.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EwmAppErrorResponse handleAnnotationValidateErrorResponse(final MethodArgumentNotValidException exception) {
        var message = new ArrayList<String>();
        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errorMessage = (errorMessage != null) ? errorMessage : NO_MESSAGE;
                    message.add(fieldName.concat(COLON).concat(errorMessage));
                });
        var stackTraceInfo = Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
        log.warn(RESPONSE.concat(BAD_REQUEST).concat(SPLITTER).concat(INVALID_PARAMETERS)
                .concat(stackTraceInfo.toString()));
        return new EwmAppErrorResponse(
                BAD_REQUEST,
                INVALID_PARAMETERS,
                message.toString(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений при получении запроса с несоответствующим форматом тела, пути или заголовков запроса.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EwmAppErrorResponse handleHttpMessageNotReadableExceptionResponse(
            final HttpMessageNotReadableException exception) {
        var stackTrace = exception.getStackTrace();
        log.warn(RESPONSE.concat(BAD_REQUEST).concat(SPLITTER).concat(NOT_READABLE_BODY)
                .concat(Arrays.toString(stackTrace)));
        return new EwmAppErrorResponse(
                BAD_REQUEST,
                NOT_READABLE_BODY,
                exception.getLocalizedMessage(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений для дополнительной валидации API запросов приложением.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({EwmAppRequestValidateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EwmAppErrorResponse handleValidationErrorResponse(final EwmAppRequestValidateException exception) {
        var stackTrace = exception.getStackTrace();
        log.warn(RESPONSE.concat(BAD_REQUEST).concat(SPLITTER).concat(INVALID_PARAMETERS).concat(SPLITTER)
                .concat(exception.getError()).concat(SPLITTER).concat(exception.getMessage()).concat(SPLITTER)
                .concat(Arrays.toString(stackTrace)));
        return new EwmAppErrorResponse(
                BAD_REQUEST,
                INVALID_PARAMETERS,
                exception.getError().concat(SPLITTER).concat(exception.getMessage()),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений при операциях, нарушающих целостность/стабильность данных или репозитория.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public EwmAppErrorResponse handleDataIntegrityViolationResponse(final DataIntegrityViolationException exception) {
        var stackTrace = exception.getStackTrace();
        log.warn(RESPONSE.concat(CONFLICT).concat(SPLITTER).concat(REQUEST_WAS_REFUSED).concat(SPLITTER)
                .concat(exception.getMostSpecificCause().getLocalizedMessage()).concat(SPLITTER)
                .concat(exception.getMessage()).concat(SPLITTER).concat(Arrays.toString(stackTrace)));
        return new EwmAppErrorResponse(
                CONFLICT,
                REQUEST_WAS_REFUSED,
                exception.getMostSpecificCause().getLocalizedMessage(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений при отсутствии запрашиваемых данных.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({EwmAppEntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public EwmAppErrorResponse handleEntityNotFoundResponse(final EwmAppEntityNotFoundException exception) {
        log.warn(RESPONSE.concat(NOT_FOUND).concat(SPLITTER).concat(exception.getError()).concat(SPLITTER)
                .concat(exception.getMessage()));
        return new EwmAppErrorResponse(
                NOT_FOUND,
                exception.getError(),
                exception.getMessage(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений при возникновении ситуаций невозможности создания или обработки данных.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({EwmAppConflictActionException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public EwmAppErrorResponse handleIncorrectDataResponse(final EwmAppConflictActionException exception) {
        log.warn(RESPONSE.concat(CONFLICT).concat(SPLITTER).concat(exception.getError()).concat(SPLITTER)
                .concat(exception.getMessage()));
        return new EwmAppErrorResponse(
                CONFLICT,
                exception.getError(),
                exception.getMessage(),
                timeStamp()
        );
    }

    /**
     * Обработчик исключений при запросах на некорректные/нереализованные эндпоинты.
     *
     * @param exception исключение
     * @return {@link EwmAppErrorResponse} с описанием ошибки и вероятных причинах
     */
    @ExceptionHandler({NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public EwmAppErrorResponse handleEndpointErrorResponse(final NoResourceFoundException exception) {
        log.warn(RESPONSE.concat(NOT_FOUND).concat(SPLITTER).concat(ENDPOINT_ERROR));
        return new EwmAppErrorResponse(
                NOT_FOUND,
                ENDPOINT_ERROR,
                exception.getLocalizedMessage(),
                timeStamp()
        );
    }

    private String timeStamp() {
        return LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

}
