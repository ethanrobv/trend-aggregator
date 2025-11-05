package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.repository.WikipediaArticleRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WikipediaService {

    private static final Logger log = LoggerFactory.getLogger(WikipediaService.class);

    private final WebClient webClient;
    private final WikipediaArticleRepository articleRepository;
    private final GdeltService gdeltService;

    public WikipediaService(WebClient.Builder webClientBuilder,
                            WikipediaArticleRepository articleRepository,
                            GdeltService gdeltService) {
        // Configure web client, need increased buffer to hold entire wiki feed API result
        final int bufSize = 5 * 1024 * 1024;
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(bufSize))
                .build();
        this.webClient = webClientBuilder
                .exchangeStrategies(strategies)
                .build();

        this.articleRepository = articleRepository;
        this.gdeltService = gdeltService;
    }

    @Transactional
    public Flux<WikipediaArticle> getTopArticles(int maxArticles) {
        return articleRepository.findAllByOrderByViewsDesc()
                .take(maxArticles);
    }

    public Mono<Json> getArticleToneChart(Long id) {
        return articleRepository.findById(id)
                .flatMap(article -> Mono.justOrEmpty(article.getToneChart()))
                .defaultIfEmpty(Json.of("{\"histogram\":[]}"));
    }

    @Transactional
    @Scheduled(
            fixedRateString = "PT1H",
            initialDelayString = "PT15M"
    )
    public Mono<Void> updateMostReadArticlesData() {
        log.info("Starting scheduled updateMostReadArticlesData()...");
        return articleRepository.deleteAll()
                .then(fetchMostReadArticlesData())
                .flatMap(articlesToSave ->
                        articleRepository.saveAll(articlesToSave)
                                .collectList()
                )
                .doOnSuccess(savedArticles ->
                    log.info("Saved {} articles", savedArticles.size())
                )
                .doOnError(error ->
                        log.error("Failed saving articles: {}", error.getMessage()))
                .then(gdeltService.processArticleGdeltToneCharts());
    }

    protected Mono<List<WikipediaArticle>> fetchMostReadArticlesData() {
        LocalDate today = LocalDate.now();
        String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = today.format(DateTimeFormatter.ofPattern("MM"));
        String day = today.format(DateTimeFormatter.ofPattern("dd"));

        String wikipediaBaseUrl = "https://api.wikimedia.org/feed/v1/wikipedia/en/featured";
        String uri = String.format("%s/%s/%s/%s", wikipediaBaseUrl, year, month, day);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseMostReadWikipediaResponse);
    }

    private List<WikipediaArticle> parseMostReadWikipediaResponse(JsonNode response) {
        List<WikipediaArticle> articles = new ArrayList<>();
        JsonNode articleNodes = response.path("mostread").path("articles");
        if (articleNodes.isArray()) {
            for (JsonNode articleNode : articleNodes) {
                String title = articleNode.path("titles")
                        .path("normalized").asText();

                long views = articleNode.path("views").asLong();

                String url = articleNode.path("content_urls")
                        .path("desktop")
                        .path("page").asText();

                JsonNode view_history = articleNode.path("view_history");
                double trend;
                int n = view_history.size();
                long sum_x = 0;
                long sum_y = 0;
                long sum_xy = 0;
                long sum_x_2 = 0;

                for (int i = 0; i < n; i++) {
                    long current_x = i + 1;
                    long current_y = view_history.get(i).path("views").asLong();

                    sum_x += current_x;
                    sum_y += current_y;
                    sum_xy += current_x * current_y;
                    sum_x_2 += current_x * current_x;
                }

                double numerator = (double) (n * sum_xy - sum_x * sum_y);
                double denominator = (double) (n * sum_x_2 - sum_x * sum_x);

                if (denominator == 0) {
                    trend = 0.0;
                } else {
                    trend = numerator / denominator;
                }

                articles.add(new WikipediaArticle(title, views, url, trend));
            }
        }

        return articles;
    }
}
