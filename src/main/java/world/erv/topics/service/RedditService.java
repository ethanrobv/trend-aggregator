package world.erv.topics.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import world.erv.topics.event.WikipediaArticlesUpdatedEvent;
import world.erv.topics.model.RedditPost;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.repository.RedditPostRepository;
import world.erv.topics.repository.WikipediaArticleRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "features.reddit-service.enabled", havingValue = "true")
public class RedditService {

    private static final Logger log = LoggerFactory.getLogger(RedditService.class);
    private final int REDDIT_CONCURRENCY = 1;
    private final long MAX_NUMBER_POSTS = 10;
    private final TransactionalOperator transactionalOperator;
    private final WebClient webClient;
    private final RedditPostRepository postRepository;
    private final WikipediaArticleRepository articleRepository;

    public RedditService(TransactionalOperator transactionalOperator,
            WebClient redditWebClient,
            RedditPostRepository postRepository,
            WikipediaArticleRepository articleRepository
    ) {
        this.transactionalOperator = transactionalOperator;
        this.webClient = redditWebClient;
        this.postRepository = postRepository;
        this.articleRepository = articleRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Reddit service enabled");
    }

    /* From REST routes */

    public Flux<RedditPost> getLatestPosts(Long wikipediaArticleId) {
        Sort defaultSort = Sort.by("postedAt").descending();
        return postRepository.getByWikipediaArticleId(wikipediaArticleId, defaultSort);
    }

    /* Automatic & event driven methods */

    @EventListener
    public Mono<Void> handleWikipediaArticlesUpdatedEvent(WikipediaArticlesUpdatedEvent event) {
        log.info("[EVENT: Consumed {}", event);
        return this.updateLatestPostsData()
                .doOnSuccess(r -> {
                    log.info("Finished Reddit processing");
                })
                .onErrorResume(error -> {
                    log.error("Reddit processing failed for event: {}", event, error);

                    return Mono.empty();
                });
    }

    /* Private helpers */

    private Mono<Void> updateLatestPostsData() {
        log.info("Updating latest reddit posts data...");

        return articleRepository.findAll()
                .delayElements(Duration.ofSeconds(2))
                .flatMap(this::fetchLatestRedditPostsData, REDDIT_CONCURRENCY)
                .flatMap(postsToSave -> {
                    if (postsToSave.isEmpty()) {
                        return Mono.empty();
                    }

                    return postRepository.saveAll(postsToSave)
                            .then()
                            .as(transactionalOperator::transactional)
                            .onErrorResume(error -> {
                                log.error("Failed to save batch of {} posts", postsToSave.size(), error);

                                return Mono.empty();
                            });
                })
                .then();
    }

    private Mono<List<RedditPost>> fetchLatestRedditPostsData(WikipediaArticle article) {
        String uri = String.format("/search.json?q=%s&type=posts&t=day", article.getTitle());

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> parseLatestPostsResponse(jsonNode, article.getId()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                        .doBeforeRetry(r -> {
                            log.warn("Hit rate limit on query '{}'. Backing off...", article.getTitle());
                        }))
                .onErrorResume(error -> {
                    log.error("Failed to fetch reddit posts for query '{}'", article.getTitle(), error);

                    return Mono.empty();
                });
    }

    private List<RedditPost> parseLatestPostsResponse(JsonNode response, Long articleId) {
        List<RedditPost> posts = new ArrayList<>();
        JsonNode postNodes = response.path("data").path("children");
        if (postNodes.isArray()) {
            for (JsonNode postNode : postNodes) {
                JsonNode postData = postNode.path("data");
                RedditPost post = new RedditPost();
                post.setSubreddit(postData.path("subreddit_name_prefixed").asText());
                post.setBody(postData.path("selftext").asText());
                post.setPostedAt(Instant.ofEpochSecond(postData.path("created_utc").asLong()));
                post.setTitle(postData.path("title").asText());
                post.setUpvotes(postData.path("ups").asLong());
                post.setUrl(String.format("https://www.reddit.com%s", postData.path("permalink").asText()));
                post.setWikipediaArticleId(articleId);

                posts.add(post);
                if (posts.size() > MAX_NUMBER_POSTS) {
                    break;
                }
            }
        }

        return posts;
    }

}
