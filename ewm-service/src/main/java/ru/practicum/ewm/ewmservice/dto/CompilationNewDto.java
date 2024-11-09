package ru.practicum.ewm.ewmservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.ewmservice.entity.CompilationEntity;
import ru.practicum.ewm.ewmservice.entity.CompilationEventRelation;
import ru.practicum.ewm.ewmservice.entity.EventEntity;
import ru.practicum.ewm.ewmservice.repository.CompilationEventRelationRepository;
import ru.practicum.ewm.ewmservice.repository.EventRepository;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link ru.practicum.ewm.ewmservice.entity.CompilationEntity}
 */
public record CompilationNewDto(
        LinkedHashSet<Long> events,
        Boolean pinned,
        @NotBlank(message = "Не указан заголовок подборки")
        @Size(min = 1, max = 50, message = "Размер сообщения не соответствует заданному диапазону")
        String title
) implements Serializable {
    public CompilationEntity toEntity(
            EventRepository eventRepository,
            CompilationEventRelationRepository relationRepository) {
        var compilationEntity = new CompilationEntity();
        HashSet<CompilationEventRelation> relations = new HashSet<>();
        if (events != null && !events.isEmpty()) {
            Set<EventEntity> eventEntities = new LinkedHashSet<>(eventRepository.findAllById(events));
            relations = new HashSet<>();
            for (var event : eventEntities) {
                var relation = new CompilationEventRelation();
                relation.setCompilation(compilationEntity);
                relation.setEvent(event);
                relations.add(relation);
            }
        }
        compilationEntity.setEvents(relations);
        compilationEntity.setTitle(title);
        compilationEntity.setPinned(pinned != null && pinned);
        return compilationEntity;
    }
}