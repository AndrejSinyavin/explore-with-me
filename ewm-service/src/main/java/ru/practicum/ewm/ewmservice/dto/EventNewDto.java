package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.io.Serializable;

/**
 * DTO for {@link EventEntity}
 */
public record EventNewDto(
        @NotBlank(message = "Не указана аннотация к событию")
        @Size(min = 20, max = 2000, message = "Размер сообщения не соответствует заданному диапазону")
        String annotation,

        @NotNull(message = "Не указана категория события")
        @Positive(message = "Величина может быть только положительным значением")
        Long category,

        @NotBlank(message = "Не указано писание события")
        @Size(min = 20, max = 7000, message = "Размер сообщения не соответствует заданному диапазону")
        String description,

        @NotBlank(message = "Не указана дата предполагаемого события ")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        String eventDate,

        @NotNull(message = "Не указаны координаты проведения мероприятия")
        @Valid
        LocationDto location,

        Boolean paid,

        @PositiveOrZero(message = "Величина не может быть отрицательным значением")
        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank(message = "Не указан заголовок события")
        @Size(min = 3, max = 120, message = "Размер сообщения не соответствует заданному диапазону")
        String title

) implements Serializable {
}