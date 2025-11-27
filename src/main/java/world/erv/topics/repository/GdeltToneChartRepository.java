package world.erv.topics.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import world.erv.topics.model.GdeltToneChart;

@Repository
public interface GdeltToneChartRepository extends ReactiveCrudRepository<GdeltToneChart, Long> {
}
