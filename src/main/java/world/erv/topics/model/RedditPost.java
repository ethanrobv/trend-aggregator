package world.erv.topics.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("reddit_posts")
public class RedditPost {

    @Id
    private Long id;
    private String body;
    private String subreddit;
    private String title;
    private long upvotes;
    private String url;

    @CreatedDate
    private Instant createdAt;

    @Column("wikipedia_article_id")
    private Long wikipediaArticleId;

    public RedditPost() {
    }

    public RedditPost(String body, String subreddit, String title, long upvotes, String url, Long wikipediaArticleId) {
        this.body = body;
        this.subreddit = subreddit;
        this.title = title;
        this.upvotes = upvotes;
        this.url = url;

        this.wikipediaArticleId = wikipediaArticleId;
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getBody() { return this.body; }
    public void setBody(String body) { this.body = body; }

    public String getSubreddit() { return this.subreddit; }
    public void setSubreddit(String subreddit) { this.subreddit = subreddit; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public long getUpvotes() { return this.upvotes; }
    public void setUpvotes(long upvotes) { this.upvotes = upvotes; }

    public String getUrl() { return this.url; }
    public void setUrl(String url) { this.url = url; }

    public Instant getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getWikipediaArticleId() { return this.wikipediaArticleId; }
    public void setWikipediaArticleId(Long wikipediaArticleId) { this.wikipediaArticleId = wikipediaArticleId; }
}
