package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.ewmservice.entity.EventSatisfactionRatingEntity;

import java.util.List;

public interface RatingSatisfactionRepository extends JpaRepository<EventSatisfactionRatingEntity, Long> {
    @Query("select e from EventSatisfactionRatingEntity e where e.event.id = ?1")
    List<EventSatisfactionRatingEntity> findByEvent_Id(Long id);

    long countByEvent_Id(Long id);
}