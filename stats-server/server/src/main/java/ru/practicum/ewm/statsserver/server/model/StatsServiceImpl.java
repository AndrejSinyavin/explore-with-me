package ru.practicum.ewm.statsserver.server.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;
import ru.practicum.ewm.statsserver.server.exception.AppBadRequestException;
import ru.practicum.ewm.statsserver.server.exception.AppInternalServiceException;
import ru.practicum.ewm.statsserver.server.exception.StatsAppAcceptedException;

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
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsRepository statsRepository;

    /**
     * Метод сохраняет в БД информации о том, что на uri конкретного сервиса был отправлен запрос пользователем.
     *
     * @param endpointHitEntity сохраняемая информация
     */
    @Override
    public void add(EndpointHitEntity endpointHitEntity) {
        try {
            var hitIsPresent = statsRepository.existsByUriAndIp(endpointHitEntity.getUri(), endpointHitEntity.getIp());
            if (hitIsPresent) {
                statsRepository.save(endpointHitEntity);
                throw new StatsAppAcceptedException(
                        this.toString(), "", ""
                );
            }
            statsRepository.save(endpointHitEntity);
        } catch (StatsAppAcceptedException exception) {
            throw exception;
        } catch (RuntimeException e) {
            throw new AppInternalServiceException(
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
                    .parse(beginArg, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                    .atZone(ZoneId.of("GMT0")));
            var end = Instant.from(LocalDateTime
                    .parse(endArg, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                    .atZone(ZoneId.of("GMT0")));
            if (begin.isAfter(end)) {
                throw new AppBadRequestException(
                        this.getClass().getName(),
                        "Неверные данные в запросе",
                        "Недопустимые границы диапазона времени"
                );
            }
            if (uris == null || uris.isEmpty()) {
                if (unique == null || !unique) {
                    stats = statsRepository.getStatsWithoutUris(begin, end);
                } else {
                    stats = statsRepository.getStatsWithoutUrisAndWithUnique(begin, end);
                }
            } else if (unique == null || !unique) {
                stats = statsRepository.getStatsWithUris(begin, end, uris);
            } else {
                stats = statsRepository.getStatsWithUrisAndWithUnique(begin, end, uris);
            }
            stats.sort(Comparator.comparingLong(ViewStatsDto::hits).reversed());
            return stats;
        } catch (RuntimeException exception) {
            throw new AppBadRequestException(
                    this.getClass().getName(),
                    "Неверные данные в запросе",
                    "Недопустимые границы диапазона времени"
            );
        }
    }
}
