package world.erv.topics.event;

import java.time.Instant;

public class WikipediaArticlesUpdatedEvent {

    private Instant createdAt;

    public WikipediaArticlesUpdatedEvent(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
