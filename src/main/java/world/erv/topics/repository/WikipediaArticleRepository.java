package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.erv.topics.model.WikipediaArticle;

@Repository
public interface WikipediaArticleRepository extends ReactiveCrudRepository<WikipediaArticle, Long> {

    /**
     *
     * @return A flux of all WikipediaArticle records ordered from most to least views.
     */
    Flux<WikipediaArticle> findAllByOrderByViewsDesc();

    /**
     *
     * @return A mono of the most recently created WikipediaArticle record.
     */
    Mono<WikipediaArticle> findTopByOrderByCreatedAtDesc();
}
