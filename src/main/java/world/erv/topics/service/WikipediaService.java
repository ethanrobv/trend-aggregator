package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import world.erv.topics.dto.WikipediaArticleDto;
import world.erv.topics.event.WikipediaFeaturedFetchEvent;
import world.erv.topics.model.Topic;
import world.erv.topics.model.WikipediaViewHistory;
import world.erv.topics.repository.TopicRepository;
import world.erv.topics.repository.WikipediaViewHistoryRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "features.wikipedia-service.enabled", havingValue = "true")
public class WikipediaService {

    private static final Logger log = LoggerFactory.getLogger(WikipediaService.class);
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher eventPublisher;
    private final WebClient webClient;
    private final TopicRepository topicRepository;
    private final WikipediaViewHistoryRepository wikipediaViewHistoryRepository;

    public WikipediaService(
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher eventPublisher,
            WebClient wikipediaWebClient,
            TopicRepository topicRepository,
            WikipediaViewHistoryRepository wikipediaViewHistoryRepository
    ) {
        this.transactionalOperator = transactionalOperator;
        this.eventPublisher = eventPublisher;
        this.webClient = wikipediaWebClient;
        this.topicRepository = topicRepository;
        this.wikipediaViewHistoryRepository = wikipediaViewHistoryRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Wikipedia service enabled");
    }

    public Flux<WikipediaViewHistory> getMostViewedArticlesInInstantRange(
            int maxArticles,
            Instant start,
            Instant end
    ) {
        return wikipediaViewHistoryRepository.findMostViewedArticlesFromDate(start, end)
                .take(maxArticles);
    }

    @Scheduled(
            fixedRateString = "PT1H"
    )
    public Mono<Void> runWikipediaService() {
        log.info("Running scheduled Wikipedia service...");

        return shouldUpdateLatestFeaturedData()
                .flatMap(shouldUpdate -> {
                    if (shouldUpdate) {
                        return updateLatestFeaturedData();
                    } else {
                        log.info("Skipping featured data update");

                        return Mono.empty();
                    }
                });
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

    private Mono<Boolean> shouldUpdateLatestFeaturedData() {
        return wikipediaViewHistoryRepository.findTopByOrderByModifiedAtDesc()
                .map(lastModified -> {
                    Duration timeSinceLastUpdate = Duration.between(lastModified.getModifiedAt(), Instant.now());

                    return timeSinceLastUpdate.toMinutes() >= 59;
                })
                .defaultIfEmpty(true);
    }

    private Mono<Void> updateLatestFeaturedData() {
        Mono<List<WikipediaArticleDto>> fetchArticlesMono = fetchLatestFeaturedData()
                .doOnError(error -> log.error("Failed to fetch featured data", error));

        return fetchArticlesMono
                .flatMap(articles -> {

                    Mono<Map<String, Topic>> topicMapMono = Flux.fromIterable(articles)
                            .flatMap(this::getOrCreateTopic)
                            .collectMap(Topic::getTitle);

                    List<String> titles = articles.stream()
                            .map(WikipediaArticleDto::title)
                            .toList();

                    return topicMapMono.flatMap(topicMap -> {

                        List<WikipediaViewHistory> histories = articles.stream()
                                .map(article -> {
                                    Topic topic = topicMap.get(article.title());
                                    WikipediaViewHistory history = new WikipediaViewHistory();
                                    history.setTopicId(topic.getId());
                                    history.setViews(article.views());
                                    history.setViewTrend(article.viewTrend());
                                    return history;
                                })
                                .toList();

                        return wikipediaViewHistoryRepository
                                .saveAll(histories)
                                .collectList();
                    })
                    .thenReturn(titles);
                })
                .as(transactionalOperator::transactional)
                .flatMap(titles -> Mono.fromRunnable(() -> {
                    WikipediaFeaturedFetchEvent event = new WikipediaFeaturedFetchEvent(
                            Instant.now(),
                            titles
                    );
                    log.info("[EVENT: Publishing {}]", event);
                    eventPublisher.publishEvent(event);
                }));
    }

    private Mono<Topic> getOrCreateTopic(WikipediaArticleDto article) {
        return topicRepository.findByTitle(article.title())
                .switchIfEmpty(topicRepository.save(new Topic(
                        article.title(),
                        article.url()
                )));
    }

    private Mono<List<WikipediaArticleDto>> fetchLatestFeaturedData() {
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = today.format(DateTimeFormatter.ofPattern("MM"));
        String day = today.format(DateTimeFormatter.ofPattern("dd"));

        String wikipediaFeaturedUrl = "/en/featured";
        String uri = String.format("%s/%s/%s/%s", wikipediaFeaturedUrl, year, month, day);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseWikipediaFeaturedResponse);
    }

    private List<WikipediaArticleDto> parseWikipediaFeaturedResponse(JsonNode response) {
        List<WikipediaArticleDto> featuredArticles = new ArrayList<>();
        JsonNode articleNodes = response.path("mostread").path("articles");
        if (articleNodes.isArray()) {
            for (JsonNode articleNode : articleNodes) {
                WikipediaArticleDto article = new WikipediaArticleDto(
                        articleNode.path("title").asText(),
                        articleNode.path("content_urls")
                                .path("desktop")
                                .path("page")
                                .asText(),
                        articleNode.path("views").asLong(),
                        getArticleTrendValue(articleNode.path("view_history"))
                );
                featuredArticles.add(article);
            }
        }

        return featuredArticles;
    }
}
