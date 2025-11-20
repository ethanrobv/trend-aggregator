package world.erv.topics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import world.erv.topics.dto.RedditPostDto;
import world.erv.topics.model.RedditPost;
import world.erv.topics.service.RedditService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RedditController {

    private final RedditService redditService;

    @Autowired
    public RedditController(RedditService redditService) { this.redditService = redditService; }

    @GetMapping("/latest-reddit-posts/{wikipedia_article_id}")
    public Mono<List<RedditPostDto>> getLatestRedditPosts(@PathVariable Long wikipedia_article_id) {
        return redditService.getLatestPosts(wikipedia_article_id)
                .map(this::convertToDto)
                .collectList();
    }

    private RedditPostDto convertToDto(RedditPost post) {
        return new RedditPostDto(
                post.getId(),
                post.getBody(),
                post.getPostedAt(),
                post.getSubreddit(),
                post.getTitle(),
                post.getUpvotes(),
                post.getUrl(),
                post.getWikipediaArticleId()
        );
    }
}
