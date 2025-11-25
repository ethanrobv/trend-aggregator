package world.erv.topics.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Tool;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import world.erv.topics.event.WikipediaArticlesUpdatedEvent;

import java.util.Collections;

@Service
@ConditionalOnProperty(name = "features.summary-service.enabled", havingValue = "true")
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);

    @Value("${google.api.key}")
    private String apiKey;

    private Client client;

    public SummaryService() {}

    @PostConstruct
    public void init() {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @EventListener
    public Mono<Void> handleWikipediaArticlesUpdatedEvent(WikipediaArticlesUpdatedEvent event) {
        log.info("[EVENT: Consumed {}", event);


    }

    private Mono<Void> updateDiscussionData() {
        return
    }

    private String getRedditSummary(String subject) {
        Tool searchTool = Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build();

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .tools(Collections.singletonList(searchTool))
                .build();

        String prompt = String.format("Search for recent discussions on Reddit about '%s'. List the 3 most " +
                "frequently mentioned facts, controversies, or recent news directly from the discussion. Do not " +
                "make conclusions which aren't supported by user discussion.", subject);

        GenerateContentResponse contentResponse = client.models.generateContent(
                "gemini-2.0-flash",
                prompt,
                contentConfig
        );

        return contentResponse.text();
    }
}
