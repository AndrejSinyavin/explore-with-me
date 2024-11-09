package ru.practicum.ewm.ewmservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.ewmservice.dto.CompilationDto;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "compilations", schema = "public")
public class CompilationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "pinned")
    Boolean pinned;

    @Column(name = "title", nullable = false, length = 50)
    String title;

    @OneToMany(mappedBy = "compilation")
    Set<CompilationEventRelation> events = new LinkedHashSet<>();

    public CompilationDto toDto() {
        return new CompilationDto(
                id,
                events.stream()
                        .map(element -> element.getEvent().toEventShortDto())
                        .collect(Collectors.toSet()),
                pinned,
                title
        );
    }
}