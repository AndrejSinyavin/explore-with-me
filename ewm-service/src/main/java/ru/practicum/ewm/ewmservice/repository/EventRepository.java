package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.ewmservice.entity.EntityEvent;

@Repository
public interface EventRepository extends JpaRepository<EntityEvent, Long> {
}