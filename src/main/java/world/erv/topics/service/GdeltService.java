package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.r2dbc.postgresql.codec.Json;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import world.erv.topics.event.WikipediaArticlesUpdatedEvent;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.repository.WikipediaArticleRepository;

import java.time.LocalDate;

@Service
@ConditionalOnProperty(name = "features.gdelt-service.enabled", havingValue = "true")
public class GdeltService {

    private static final Logger log = LoggerFactory.getLogger(GdeltService.class);
    private static final String GDELT_TIMESPAN = "7d";
    private final int GDELT_CONCURRENCY = 5;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final WikipediaArticleRepository articleRepository;

    public GdeltService(TransactionalOperator transactionalOperator,
                        ObjectMapper objectMapper,
                        WebClient gdeltWebClient,
                        WikipediaArticleRepository articleRepository
    ) {
        this.transactionalOperator = transactionalOperator;
        this.objectMapper = objectMapper;
        this.webClient = gdeltWebClient;
        this.articleRepository = articleRepository;
    }

    @PostConstruct
    public void init() {
        log.info("GDELT service enabled");
    }

    /* Automatic & event driven methods */

    @EventListener
    public Mono<Void> handleWikipediaArticlesUpdatedEvent(WikipediaArticlesUpdatedEvent event) {
        log.info("[EVENT: Consumed {}]", event);

        return this.processGdeltToneCharts()
                .doOnSuccess(r -> {
                    log.info("Finished GDELT processing");
                })
                .onErrorResume(error -> {
                    log.error("GDELT processing failed for event {}", event, error);
                    return Mono.empty();
                });
    }

    /* Private helpers */

    private Mono<Void> processGdeltToneCharts() {
        log.info("Updating tone chart data...");

        return articleRepository.findAll()
                .flatMap(article -> this.fetchAndSetToneChart(article)
                        .flatMap(articleRepository::save)
                        .as(transactionalOperator::transactional)
                        .onErrorResume(error -> {
                            log.error("Failed to update tone chart data for subject '{}'", article.getTitle(), error);

                            return Mono.empty();
                        }), GDELT_CONCURRENCY)
                .then();
    }

    private Mono<WikipediaArticle> fetchAndSetToneChart(WikipediaArticle article) {
        String query = article.getTitle();
        String uri = String.format("?query=\"%s\"&mode=tonechart&format=json&timespan=%s", query, GDELT_TIMESPAN);
        Mono<String> chartJsonMono = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseToneChartResponse)
                .onErrorReturn(buildEmptyToneChartJson());

        return chartJsonMono
                .map(jsonString -> {
                    article.setToneChart(Json.of(jsonString));

                    return article;
                });
    }

    private String parseToneChartResponse(JsonNode response) {
        JsonNode toneChartArray = response.path("tonechart");

        ObjectNode root = objectMapper.createObjectNode();
        root.put("timespan", GDELT_TIMESPAN);
        root.put("query_date", LocalDate.now().toString());

        if (toneChartArray.isMissingNode() || !toneChartArray.isArray() || toneChartArray.isEmpty()) {
            root.set("histogram", objectMapper.createArrayNode());
        } else {
            ArrayNode histogram = objectMapper.createArrayNode();
            for (JsonNode binEntry : toneChartArray) {
                ObjectNode newEntry = objectMapper.createObjectNode();
                newEntry.set("bin", binEntry.path("bin"));
                newEntry.set("count", binEntry.path("count"));
                histogram.add(newEntry);
            }

            root.set("histogram", histogram);
        }

        return root.toString();
    }

    private String buildEmptyToneChartJson() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("timespan", GDELT_TIMESPAN);
        root.put("query_date", LocalDate.now().toString());
        root.set("histogram", objectMapper.createArrayNode());

        return root.toString();
    }
}
