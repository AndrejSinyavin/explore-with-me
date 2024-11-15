package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.ewmservice.entity.EventState;
import ru.practicum.ewm.ewmservice.entity.EventRatesEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RatingStatRepository extends JpaRepository<EventRatesEntity, Long> {
    @Query("""
            select e from EventRatesEntity e
            where e.event.eventDate >= :eventDate and e.event.state = :state
            order by e.expectationRate desc limit :top""")
    List<EventRatesEntity> findExpectationsTop(
            @Param("eventDate") Instant eventDate,
            @Param("state") EventState state,
            @Param("top") Integer top);

    Optional<EventRatesEntity> findByEvent_Id(Long id);
}