package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import world.erv.topics.model.RedditPost;

@Repository
public interface RedditPostRepository extends ReactiveCrudRepository<RedditPost, Long> {

    /**
     * Returns all reddit posts found from searching a wikipedia article's title.
     */
    Flux<RedditPost> getByWikipediaArticleId(Long wikipediaArticleId);
}
