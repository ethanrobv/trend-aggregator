CREATE TABLE reddit_posts (
    id BIGSERIAL PRIMARY KEY,
    body TEXT,
    subreddit VARCHAR(255),
    title TEXT,
    upvotes BIGINT,
    url VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    wikipedia_article_id BIGSERIAL
);

ALTER TABLE reddit_posts
    ADD CONSTRAINT fk_reddit_posts_wikipedia_articles
    FOREIGN KEY (wikipedia_article_id)
    REFERENCES wikipedia_articles (id)
    ON DELETE CASCADE;
