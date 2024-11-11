package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.entity.EventEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    @Query("select e from EventEntity e where e.initiator.id = :id order by e.id limit :pageSize offset :pageFrom")
    List<EventFullDto> findByInitiator_IdOrderByIdAsc(Long id, Integer pageFrom, Integer pageSize);

    @Query("select e from EventEntity e where e.id = ?1 and e.initiator.id = ?2")
    Optional<EventEntity> findByIdAndInitiator_Id(Long eId, Long uId);

    @Transactional
    @Modifying
    @Query("update EventEntity e set e.confirmedRequests = ?1 where e.id = ?2")
    void updateConfirmedRequestsById(Long confirmedRequests, Long id);

    @Transactional
    @Modifying
    @Query("update EventEntity e set e.views = (e.views + 1) where e.id = ?1")
    void updateViewsById(Long id);
}