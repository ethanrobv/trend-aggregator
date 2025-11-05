package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import world.erv.topics.model.WikipediaArticle;

@Repository
public interface WikipediaArticleRepository extends ReactiveCrudRepository<WikipediaArticle, Long> {

    /**
     * Returns all articles in rank order (hi -> lo).
     */
    Flux<WikipediaArticle> findAllByOrderByViewsDesc();
}
