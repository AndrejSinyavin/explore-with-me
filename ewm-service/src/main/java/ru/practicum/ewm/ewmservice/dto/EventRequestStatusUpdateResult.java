package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;
import java.util.List;

public record EventRequestStatusUpdateResult(
        List<ParticipationRequestDto> confirmedRequests,
        List<ParticipationRequestDto> rejectedRequests
) implements Serializable {
}
