package ru.practicum.ewm.statsserver.server.model;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.statsserver.commondto.HitDto;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;
import ru.practicum.ewm.statsserver.server.exception.AppBadRequestException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Реализация интерфейса {@link StatsService}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatsServiceImpl implements StatsService {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsRepository statsRepository;

    /**
     * Запись в репозиторий информации об успешной обработке обращения по конкретному эндпоинту основного сервиса
     *
     * @param hitDto сохраняемая информация
     * @return true, если записан запрос с нового IP
     */
    @Override
    public boolean add(HitDto hitDto) {
        var isUniqueHit = !statsRepository.isHitExists(hitDto.uri(), hitDto.ip());
        statsRepository.save(new HitEntity(0L, hitDto.app(), hitDto.uri(), hitDto.ip(),
                Instant.from(LocalDateTime.parse(
                        hitDto.timestamp(),
                        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)).atZone(ZoneId.of("UTC")))));
        return isUniqueHit;
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
        List<ViewStatsDto> stats;
        Instant end;
        Instant begin;
        if ((beginArg == null && endArg != null) || (beginArg != null && endArg == null)) {
            throw new AppBadRequestException(
                    this.getClass().getName(),
                    "Неверные данные в запросе",
                    "Диапазон для поиска либо должен быть задан полностью, либо отсутствовать"
            );
        } else if (beginArg == null) {
            begin = Instant.EPOCH;
            end = Instant.now(Clock.systemUTC());
        } else {
            begin = Instant.from(LocalDateTime
                    .parse(beginArg, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                    .atZone(ZoneId.of("UTC")));
            end = Instant.from(LocalDateTime
                    .parse(endArg, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                    .atZone(ZoneId.of("UTC")));
        }
        if (begin.isAfter(end)) {
            throw new AppBadRequestException(
                    this.getClass().getName(),
                    "Неверные данные в запросе",
                    "Недопустимые границы временного диапазона для поиска:" +
                            " начальная граница не может быть позже конечной"
            );
        }
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                stats = statsRepository.getStatsWithoutUrisAndWithUnique(begin, end);
            } else {
                stats = statsRepository.getStatsWithoutUris(begin, end);
            }
        } else if (unique) {
            stats = statsRepository.getStatsWithUrisAndWithUnique(begin, end, uris);
        } else {
            stats = statsRepository.getStatsWithUris(begin, end, uris);
        }
        stats.sort(Comparator.comparingLong(ViewStatsDto::hits).reversed());
        return stats;
    }
}
