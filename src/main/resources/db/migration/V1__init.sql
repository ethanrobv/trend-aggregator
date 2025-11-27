CREATE TABLE IF NOT EXISTS topics
(
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255)             NOT NULL,
    wikipedia_url TEXT,
    modified_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS wikipedia_view_history
(
    id          BIGSERIAL PRIMARY KEY,
    topic_id    BIGSERIAL,
    view_trend  DOUBLE PRECISION,
    views       BIGINT,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE wikipedia_view_history
    ADD CONSTRAINT fk_wikipedia_view_history_topics
        FOREIGN KEY (topic_id)
            REFERENCES topics (id)
            ON DELETE CASCADE;

CREATE INDEX idx_topic_views_date ON wikipedia_view_history (topic_id, created_at);

CREATE TABLE IF NOT EXISTS gdelt_tone_charts
(
    id          BIGSERIAL PRIMARY KEY,
    topic_id    BIGSERIAL,
    tone_chart  JSONB,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE gdelt_tone_charts
    ADD CONSTRAINT fk_gdelt_tone_charts_topics
        FOREIGN KEY (topic_id)
            REFERENCES topics (id)
            ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS reddit_summaries
(
    id          BIGSERIAL PRIMARY KEY,
    topic_id    BIGSERIAL,
    summary     TEXT,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reddit_summaries
    ADD CONSTRAINT fk_reddit_summaries_topics
        FOREIGN KEY (topic_id)
            REFERENCES topics (id)
            ON DELETE CASCADE;
