package world.erv.topics.dto;

public record RedditPostDto(
        Long id,
        String body,
        String subreddit,
        String title,
        long upvotes,
        String url,
        Long wikipediaArticleId) {
}
