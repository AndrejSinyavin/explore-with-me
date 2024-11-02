package ru.practicum.ewm.ewmservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Clock.systemUTC;

@Getter
@Setter
@Entity
@Table(name = "events", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "category", nullable = false)
    EntityCategory entityCategory;

    @Column(name = "confirmed_requests")
    Long confirmedRequests;

    @Column(name = "created_on")
    Instant createdOn;

    @Column(name = "description", length = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    Instant eventDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "initiator", nullable = false)
    EntityUser initiator;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "location", nullable = false)
    EntityEventLocation location;

    @Column(name = "paid", nullable = false)
    Boolean paid = true;

    @Column(name = "participant_limit")
    Integer participantLimit = 0;

    @Column(name = "published_on")
    Instant publishedOn;

    @Column(name = "request_moderation")
    Boolean requestModeration = true;

    @Column(name = "state", columnDefinition = "Статус события")
    String state;

    @Column(name = "title", nullable = false, length = 120)
    String title;

    @Column(name = "views")
    Long views;

    public EventFullDto toEventFullDto() {
        return new EventFullDto(
                id,
                annotation,
                entityCategory.toCategoryDto(),
                confirmedRequests,
                localDataTimeOf(createdOn),
                description,
                localDataTimeOf(eventDate),
                initiator.toUserShortDto(),
                location.toDto(),
                paid,
                participantLimit,
                localDataTimeOf(publishedOn),
                requestModeration,
                title,
                views,
                state
        );
    }

    public String localDataTimeOf(Instant instant) {
        if (instant == null) {
            return "";
        } else {
            return LocalDateTime.ofInstant(
                    instant,
                    systemUTC().getZone()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        }
    }

}