package world.erv.topics.event;

import java.time.Instant;
import java.util.List;

public class WikipediaFeaturedFetchEvent {

    private Instant createdAt;
    private List<String> topicTitles;

    public WikipediaFeaturedFetchEvent(Instant createdAt, List<String> topicTitles) {
        this.createdAt = createdAt;
        this.topicTitles = topicTitles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTopicTitles() {
        return topicTitles;
    }

    public void setTopicTitles(List<String> topicTitles) {
        this.topicTitles = topicTitles;
    }
}
