package ru.practicum.ewm.ewmservice.service;

import jakarta.validation.Valid;
import ru.practicum.ewm.ewmservice.dto.EventFullDto;
import ru.practicum.ewm.ewmservice.dto.EventNewDto;

import java.time.Instant;

public interface ApiEvent {

    EventFullDto addEvent(Long uId, @Valid EventNewDto eventNewDto, Instant eventDateTime);
}
