package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.repository.WikipediaArticleRepository;

import java.time.LocalDate;

@Service
public class GdeltService {

    private static final Logger log = LoggerFactory.getLogger(GdeltService.class);
    private static final String GDELT_TIMESPAN = "7d";

    private final WebClient webClient;
    private final WikipediaArticleRepository articleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GdeltService(WebClient.Builder webClientBuilder, WikipediaArticleRepository articleRepository) {
        this.webClient = webClientBuilder.build();
        this.articleRepository = articleRepository;
    }

    public Mono<Void> processArticleGdeltToneCharts() {
        log.info("Starting processArticleGdeltToneCharts");
        int concurrency = 5;
        return articleRepository.findAll()
                .flatMap(this::fetchAndSetToneChart, concurrency)
                .flatMap(articleRepository::save)
                .then()
                .doOnSuccess(updatedArticles -> log.info("Finished fetching tone chart data."))
                .doOnError(error ->
                        log.error("Failed updating articles: {}", error.getMessage()));
    }

    private Mono<WikipediaArticle> fetchAndSetToneChart(WikipediaArticle article) {
        String query = article.getTitle();
        String gdeltApiUrl = "https://api.gdeltproject.org/api/v2/doc/doc";
        String uri = String.format("%s?query=\"%s\"&mode=tonechart&format=json&timespan=%s",
                gdeltApiUrl, query, GDELT_TIMESPAN);

        Mono<String> chartJsonMono = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseToneChartResponse);

        return chartJsonMono
                .onErrorReturn(buildEmptyToneChartJson())
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
