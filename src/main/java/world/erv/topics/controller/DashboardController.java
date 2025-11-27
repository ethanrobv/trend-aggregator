package world.erv.topics.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import world.erv.topics.dto.TopicDto;
import world.erv.topics.model.Topic;
import world.erv.topics.model.WikipediaViewHistory;
import world.erv.topics.repository.TopicRepository;
import world.erv.topics.service.WikipediaService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "features.wikipedia-service.enabled", havingValue = "true")
public class DashboardController {

    private final TopicRepository topicRepository;
    private final WikipediaService wikipediaService;

    public DashboardController(
            TopicRepository topicRepository,
            WikipediaService wikipediaService
    ) {
        this.topicRepository = topicRepository;
        this.wikipediaService = wikipediaService;
    }

    /**
     * Returns the top 50 trending topics for a specific date.
     */
    @GetMapping("/trending-topics/{year}/{month}/{day}")
    public Mono<List<TopicDto>> getTrendingTopics(
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day
    ) {
        // 1. Calculate the Instant range for the requested day (UTC)
        LocalDate date = LocalDate.of(year, month, day);
        var start = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        var end = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

        // 2. Get the history records (Views + Trend + TopicID)
        return wikipediaService.getMostViewedArticlesInInstantRange(50, start, end)
                .collectList()
                .flatMap(histories -> {
                    if (histories.isEmpty()) {
                        return Mono.just(List.of());
                    }

                    // 3. Extract Topic IDs to fetch names/URLs
                    Set<Long> topicIds = histories.stream()
                            .map(WikipediaViewHistory::getTopicId)
                            .collect(Collectors.toSet());

                    // 4. Fetch Topics and Map them by ID
                    return topicRepository.findAllById(topicIds)
                            .collectMap(Topic::getId)
                            .map(topicMap -> {
                                // 5. Merge History + Topic into DTOs
                                return histories.stream()
                                        .filter(h -> topicMap.containsKey(h.getTopicId()))
                                        .map(h -> {
                                            Topic t = topicMap.get(h.getTopicId());
                                            return new TopicDto(
                                                    t.getId(),
                                                    t.getTitle(),
                                                    t.getWikipediaUrl(),
                                                    h.getViews(),
                                                    h.getViewTrend()
                                            );
                                        })
                                        .collect(Collectors.toList());
                            });
                });
    }
}
