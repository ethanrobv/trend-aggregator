package world.erv.topics.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.erv.topics.model.WikipediaViewHistory;

import java.time.Instant;

@Repository
public interface WikipediaViewHistoryRepository extends ReactiveCrudRepository<WikipediaViewHistory, Long> {

    @Query("SELECT * " +
            "FROM wikipedia_view_history " +
            "WHERE created_at >= :start AND created_at < :end " +
            "ORDER BY views DESC")
    public Flux<world.erv.topics.model.WikipediaViewHistory> findMostViewedArticlesFromDate(
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    public Mono<world.erv.topics.model.WikipediaViewHistory> findTopByOrderByModifiedAtDesc();
}
