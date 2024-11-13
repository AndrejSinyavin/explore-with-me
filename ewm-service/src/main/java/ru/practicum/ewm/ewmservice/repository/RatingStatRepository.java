package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.ewmservice.entity.EventState;
import ru.practicum.ewm.ewmservice.entity.EventStatsEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RatingStatRepository extends JpaRepository<EventStatsEntity, Long> {
    @Query("""
            select e from EventStatsEntity e
            where e.event.eventDate >= :eventDate and e.event.state = :state
            order by e.expectationRate desc limit :top""")
    List<EventStatsEntity> findExpectationsTop(
            @Param("eventDate") Instant eventDate,
            @Param("state") EventState state,
            @Param("top") Integer top);

    Optional<EventStatsEntity> findByEvent_Id(Long id);
}