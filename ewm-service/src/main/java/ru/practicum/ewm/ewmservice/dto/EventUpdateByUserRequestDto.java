package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.io.Serializable;

/**
 * DTO for {@link EventEntity}
 */
public record EventUpdateByUserRequestDto(

        @Size(min = 20, max = 2000, message = "Размер сообщения не соответствует заданному диапазону")
        String annotation,

        @Positive(message = "Величина может быть только положительным значением")
        Integer category,

        @Size(min = 20, max = 7000, message = "Размер сообщения не соответствует заданному диапазону")
        String description,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        String eventDate,

        @Valid
        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Величина не может быть отрицательным значением")
        Integer participantLimit,

        Boolean requestModeration,

        @Size(min = 3, max = 30, message = "Размер сообщения не соответствует заданному диапазону")
        String stateAction,

        @Size(min = 3, max = 120, message = "Размер сообщения не соответствует заданному диапазону")
        String title

) implements Serializable {
}
