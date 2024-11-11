package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.ewmservice.entity.CompilationEventRelation;

public interface CompilationEventRelationRepository extends JpaRepository<CompilationEventRelation, Long> {
}