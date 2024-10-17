package ru.practicum.model.server;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.exception.InternalServiceException;
import ru.practicum.model.dto.ViewStatsDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static ru.practicum.StatsServiceApplication.dateTimePattern;

/**
 * Реализация интерфейса {@link StatsService}
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsServiceImpl implements StatsService {
    StatsRepository statsRepository;

    /**
     * Метод сохраняет в БД информации о том, что на uri конкретного сервиса был отправлен запрос пользователем.
     *
     * @param endpointHitEntity сохраняемая информация
     */
    @Override
    public void add(EndpointHitEntity endpointHitEntity) {
        try {
            statsRepository.save(endpointHitEntity);
        } catch (Exception e) {
            throw new InternalServiceException(
                    this.statsRepository.getClass().getName(),
                    "Запись статистики не выполнена",
                    e.getMessage()
            );
        }
    }

    /**
     * Метод получает из БД статистику о запросах пользователей
     *
     * @param beginArg начало периода статистики (включительно)
     * @param endArg конец периода статистики (включительно)
     * @param uris список эндпоинтов, по которым собирается статистика, null или пустой список -
     *            для статистики по всем эндпоинтам
     * @param unique true - подсчитывать все запросы на эндпоинт, false или null - не подсчитывать повторные запросы с
     *               IP адресов, которые уже обращались к эндпоинту
     * @return список со статистикой
     */
    @Override
    public List<ViewStatsDto> getStats(String beginArg, String endArg, List<String> uris, Boolean unique) {
        try {
            List<ViewStatsDto> stats;
            var begin = Instant.from(LocalDateTime
                    .parse(beginArg, DateTimeFormatter.ofPattern(dateTimePattern))
                    .atZone(ZoneId.of("GMT0")));
            var end = Instant.from(LocalDateTime
                    .parse(endArg, DateTimeFormatter.ofPattern(dateTimePattern))
                    .atZone(ZoneId.of("GMT0")));
            if (uris == null || uris.isEmpty()) {
                if (unique == null || !unique) {
                    stats = statsRepository.getStatsWithoutUris(begin, end);
                } else {
                    stats =  statsRepository.getStatsWithoutUrisAndWithUnique(begin, end);
                }
            } else if (unique == null || !unique) {
                stats =  statsRepository.getStatsWithUris(begin, end, uris);
            } else {
                stats =  statsRepository.getStatsWithUrisAndWithUnique(begin, end, uris);
            }
            stats.sort(Comparator.comparingLong(ViewStatsDto::hits).reversed());
            return stats;
        } catch (Exception e) {
            throw new InternalServiceException(
                    this.statsRepository.getClass().getName(),
                    "Чтение статистики не выполнено",
                    e.getMessage()
            );
        }
    }
}
