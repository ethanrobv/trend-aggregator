package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import world.erv.topics.model.RedditSummary;

@Repository
public interface RedditSummaryRepository extends ReactiveCrudRepository<RedditSummary, Long> {
}
