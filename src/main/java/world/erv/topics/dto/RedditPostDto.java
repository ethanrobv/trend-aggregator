package world.erv.topics.dto;

import java.time.Instant;

public record RedditPostDto(
        Long id,
        String body,
        Instant postedAt,
        String subreddit,
        String title,
        long upvotes,
        String url,
        Long wikipediaArticleId) {
}
