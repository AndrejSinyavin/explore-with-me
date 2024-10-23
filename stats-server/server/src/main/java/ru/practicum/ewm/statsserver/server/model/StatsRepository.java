package ru.practicum.ewm.statsserver.server.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.statsserver.commondto.ViewStatsDto;

import java.time.Instant;
import java.util.List;

/**
 * JPA-репозиторий сервиса работы со статистикой
 */
@Repository
public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {
    @Query("""
            select new ru.practicum.ewm.statsserver.commondto.ViewStatsDto(e.app, e.uri, count(e.ip))
            from EndpointHitEntity e
            where (e.timestamp >= :begin) and (e.timestamp <= :end) and (e.uri in :uris)
            group by e.app, e.uri
            order by e.app, e.uri""")
    List<ViewStatsDto> getStatsWithUris(
            @Param("begin") Instant begin,
            @Param("end") Instant end,
            @Param("uris") List<String> uris);

    @Query("""
            select new ru.practicum.ewm.statsserver.commondto.ViewStatsDto(e.app, e.uri, count(e.ip))
            from EndpointHitEntity e
            where e.timestamp >= :begin and e.timestamp <= :end
            group by e.app, e.uri
            order by e.app, e.uri""")
    List<ViewStatsDto> getStatsWithoutUris(
            @Param("begin") Instant begin,
            @Param("end") Instant end);

    @Query("""
            select new ru.practicum.ewm.statsserver.commondto.ViewStatsDto(e.app, e.uri, count(distinct e.ip))
            from EndpointHitEntity e
            where e.timestamp >= :begin and e.timestamp <= :end and (e.uri in :uris)
            group by e.app, e.uri
            order by e.app, e.uri""")
    List<ViewStatsDto> getStatsWithUrisAndWithUnique(
            @Param("begin") Instant begin,
            @Param("end") Instant end,
            @Param("uris") List<String> uris);

    @Query("""
            select new ru.practicum.ewm.statsserver.commondto.ViewStatsDto(e.app, e.uri, count(distinct e.ip))
            from EndpointHitEntity e
            where e.timestamp >= :begin and e.timestamp <= :end
            group by e.app, e.uri
            order by e.app, e.uri""")
    List<ViewStatsDto> getStatsWithoutUrisAndWithUnique(
            @Param("begin") Instant begin,
            @Param("end") Instant end);

}