package ru.practicum.ewm.ewmservice.service;

import ru.practicum.ewm.ewmservice.dto.CompilationDto;
import ru.practicum.ewm.ewmservice.dto.CompilationNewDto;
import ru.practicum.ewm.ewmservice.dto.CompilationUpdateRequestDto;

import java.util.List;
import java.util.Optional;

public interface CompilationApiService {
    CompilationDto addCompilation(CompilationNewDto dto);

    Optional<CompilationDto> getCompilationById(Long cpId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    void deleteCompilation(Long cpId);

    CompilationDto updateCompilation(Long cpId, CompilationUpdateRequestDto dto);
}
