package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import world.erv.topics.model.Topic;

@Repository
public interface TopicRepository extends ReactiveCrudRepository<Topic, Long> {

    public Mono<Topic> findByTitle(String title);
}
