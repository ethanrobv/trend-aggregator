package world.erv.topics.service;

import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import world.erv.topics.event.WikipediaFeaturedFetchEvent;
import world.erv.topics.model.GdeltToneChart;
import world.erv.topics.repository.GdeltToneChartRepository;
import world.erv.topics.repository.TopicRepository;

import java.util.List;

@Service
public class GdeltService {

    private static final Logger log = LoggerFactory.getLogger(GdeltService.class);
    private final TransactionalOperator transactionalOperator;
    private final WebClient webClient;
    private final GdeltToneChartRepository gdeltToneChartRepository;
    private final TopicRepository topicRepository;

    public GdeltService(
            TransactionalOperator transactionalOperator,
            WebClient gdeltWebClient,
            GdeltToneChartRepository gdeltToneChartRepository,
            TopicRepository topicRepository
    ) {
        this.transactionalOperator = transactionalOperator;
        this.webClient = gdeltWebClient;
        this.gdeltToneChartRepository = gdeltToneChartRepository;
        this.topicRepository = topicRepository;
    }

    @EventListener
    public Mono<Void> handleWikipediaFeaturedFetchEvent(WikipediaFeaturedFetchEvent event) {
        log.info("[EVENT: Consumed {}", event);

        return runGdeltService(event.getTopicTitles());
    }

    public Mono<Void> runGdeltService(List<String> topicTitles) {

    }

    public Mono<GdeltToneChart> fetchGdeltToneChart(String topicTitle, String timespan) {
        String uri = String.format("?query=\"%s\"&mode=tonechart&format=json&timespan=%s", topicTitle, timespan);
    }
}
