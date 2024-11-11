package ru.practicum.ewm.ewmservice.dto;

import java.io.Serializable;
import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,
        String status
) implements Serializable {
}
