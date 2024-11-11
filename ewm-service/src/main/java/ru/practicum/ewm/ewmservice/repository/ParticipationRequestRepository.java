package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestEntity;
import ru.practicum.ewm.ewmservice.entity.ParticipationRequestState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequestEntity, Long> {

    @Query("""
            select p from ParticipationRequestEntity p
            where p.event.id = ?1 and p.id in ?2
            order by p.requester.id""")
    List<ParticipationRequestEntity> getAllFromRequestTargetList(Long eid, Collection<Long> ids);

    Optional<ParticipationRequestEntity> findByIdAndRequesterId(Long rId, Long uId);

    List<ParticipationRequestEntity> findAllByEventIdOrderByRequesterId(Long eId);

    List<ParticipationRequestEntity> findAllByRequesterIdOrderById(Long uId);

    long countByEvent_IdAndStatus(Long id, ParticipationRequestState status);
}