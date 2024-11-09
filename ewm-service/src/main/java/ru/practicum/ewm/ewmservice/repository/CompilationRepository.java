package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.ewmservice.entity.CompilationEntity;
import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.util.List;
import java.util.Set;

public interface CompilationRepository extends JpaRepository<CompilationEntity, Long> {

    @Query("select c from CompilationEntity c where c.pinned = ?1 order by c.id limit ?2 offset ?3")
    List<CompilationEntity> findAllPinned(Boolean pinned, Integer limit, Integer offset);

    @Transactional
    @Modifying
    @Query("update CompilationEntity c set c.events = ?1, c.pinned = ?2, c.title = ?3 where c.id = ?4")
    CompilationEntity updateEventsAndPinnedAndTitleById(
            @Nullable Set<EventEntity> events,
            @Nullable Boolean pinned,
            @Nullable String title,
            Long id);
}