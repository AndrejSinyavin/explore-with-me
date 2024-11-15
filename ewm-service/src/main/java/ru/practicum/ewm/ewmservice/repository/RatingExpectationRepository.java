package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.ewmservice.entity.EventExpectationRatingEntity;

public interface RatingExpectationRepository extends JpaRepository<EventExpectationRatingEntity, Long> {
    long countByEvent_Id(Long id);
}