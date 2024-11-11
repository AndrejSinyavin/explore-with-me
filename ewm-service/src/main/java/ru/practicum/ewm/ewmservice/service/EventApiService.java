package ru.practicum.ewm.ewmservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.ewmservice.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.ewmservice.dto.EventShortDto;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByAdminRequestDto;
import ru.practicum.ewm.ewmservice.dto.EventUpdateByUserRequestDto;
import ru.practicum.ewm.ewmservice.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface EventApiService {

    EventFullDto addEvent(Long uId, @Valid EventNewDto eventNewDto);

    List<EventFullDto> getEvents(Long uId, Integer pageFrom, Integer pageSize);

    EventFullDto getEventByIdAndUserId(Long eId, Long uId);

    EventFullDto authorUpdateEvent(Long uId, Long eId, @Valid EventUpdateByUserRequestDto eventPatchDto);

    ParticipationRequestDto createParticipationRequest(Long uId, Long eId);

    EventFullDto adminUpdateEvent(Long eId, EventUpdateByAdminRequestDto updateDto);

    EventRequestStatusUpdateResult updateRequestStatuses(
            EventRequestStatusUpdateRequest statuses, Long uId, Long eId);

    ParticipationRequestDto cancelRequest(Long rId, Long uId);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long eId, Long uId);

    List<ParticipationRequestDto> getAllUserRequests(Long uId);

    EventFullDto getFullEvent(Long eId);

    List<EventShortDto> getEventsByCriteria(HttpServletRequest request, Map<String, String> params);

    List<EventFullDto> findAllStats(HttpServletRequest request, Map<String, String> params);

    void addReview(Long eId);
}
