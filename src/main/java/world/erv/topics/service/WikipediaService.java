package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.r2dbc.postgresql.codec.Json;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.erv.topics.event.WikipediaArticlesUpdatedEvent;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.repository.WikipediaArticleRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "features.wikipedia-service.enabled", havingValue = "true")
public class WikipediaService {

    private static final Logger log = LoggerFactory.getLogger(WikipediaService.class);
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher eventPublisher;
    private final WebClient webClient;
    private final WikipediaArticleRepository articleRepository;

    public WikipediaService(TransactionalOperator transactionalOperator,
                            ApplicationEventPublisher eventPublisher,
                            WikipediaArticleRepository articleRepository,
                            WebClient wikipediaWebClient
    ) {
        this.transactionalOperator = transactionalOperator;
        this.eventPublisher = eventPublisher;
        this.webClient = wikipediaWebClient;
        this.articleRepository = articleRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Wikipedia service enabled");
    }

    /* From REST routes */

    public Flux<WikipediaArticle> getTopArticles(int maxArticles) {
        return articleRepository.findAllByOrderByViewsDesc()
                .take(maxArticles);
    }

    public Mono<Json> getArticleToneChart(Long id) {
        return articleRepository.findById(id)
                .flatMap(article -> Mono.justOrEmpty(article.getToneChart()))
                .defaultIfEmpty(Json.of("{\"histogram\":[]}"));
    }

    /* Automatic & event driven methods */

    @Scheduled(
            fixedRateString = "PT1H",
            initialDelayString = "PT10S"
    )
    public Mono<Void> updateMostReadArticlesData() {
        return articleRepository.findTopByOrderByCreatedAtDesc()
                .map(latestArticle -> {
                    Duration timeSinceLastFetch = Duration.between(latestArticle.getCreatedAt(), Instant.now());
                    boolean isStale = timeSinceLastFetch.toMinutes() >= 59;
                    if (!isStale) {
                        log.info("Skipping most read article update: Data last fetched {} minutes ago",
                                timeSinceLastFetch.toMinutes());
                    }

                    return isStale;
                })
                .defaultIfEmpty(true)
                .filter(shouldUpdate -> shouldUpdate)
                .flatMap(r -> {
                    return doMostReadArticlesUpdate();
                });
    }

    /* Private helpers */

    private Mono<Void> doMostReadArticlesUpdate() {
        log.info("Updating wikipedia most viewed article data...");
        Mono<List<WikipediaArticle>> fetchArticlesMono = fetchMostReadArticlesData()
                .doOnError(error -> {
                    log.error("Failed to fetch most read articles data", error);
                });
        Mono<Void> publishArticlesUpdatedEvent = Mono.fromRunnable(() -> {
            WikipediaArticlesUpdatedEvent event = new WikipediaArticlesUpdatedEvent(Instant.now());
            log.info("[EVENT: Publishing {}]", event);
            eventPublisher.publishEvent(event);
        });

        return fetchArticlesMono
                .flatMap(articlesToSave -> {
                    Mono<List<WikipediaArticle>> articleRefreshTransaction =
                            articleRepository.deleteAll()
                                    .then(articleRepository.saveAll(articlesToSave).collectList())
                                    .doOnSuccess(savedArticles -> {
                                        log.info("Fetched and saved {} articles", savedArticles.size());
                                    });

                    return articleRefreshTransaction
                            .as(transactionalOperator::transactional)
                            .then(publishArticlesUpdatedEvent);
                })
                .doOnError(error -> {
                    log.error("Failed to update most read articles data", error);
                });
    }

    private Mono<List<WikipediaArticle>> fetchMostReadArticlesData() {
        LocalDate today = LocalDate.now();
        String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = today.format(DateTimeFormatter.ofPattern("MM"));
        String day = today.format(DateTimeFormatter.ofPattern("dd"));

        String wikipediaFeaturedUrl = "/en/featured";
        String uri = String.format("%s/%s/%s/%s", wikipediaFeaturedUrl, year, month, day);

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
                WikipediaArticle article = new WikipediaArticle();
                article.setTitle(articleNode.path("titles").path("normalized").asText());
                article.setUrl(articleNode.path("content_urls")
                        .path("desktop")
                        .path("page")
                        .asText());
                article.setViews(articleNode.path("views").asLong());
                article.setViewTrend(getArticleTrendValue(articleNode.path("view_history")));

                articles.add(article);
            }
        }

        return articles;
    }

    private static double getArticleTrendValue(JsonNode view_history) {
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

        return trend;
    }
}
