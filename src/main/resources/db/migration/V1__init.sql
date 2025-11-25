CREATE TABLE IF NOT EXISTS topics (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS wikipedia_views_snapshots (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGSERIAL,
    title VARCHAR(255) NOT NULL,
    url VARCHAR,
    view_trend DOUBLE PRECISION,
    views BIGINT,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE wikipedia_views_snapshots
    ADD CONSTRAINT fk_wikipedia_views_snapshots_topics
    FOREIGN KEY (topic_id)
    REFERENCES topics (id)
    ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS gdelt_tone_chart_snapshots (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGSERIAL,
    tone_chart JSONB,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE gdelt_tone_chart_snapshots
    ADD CONSTRAINT fk_gdelt_tone_chart_snapshots_topics
    FOREIGN KEY (topic_id)
    REFERENCES topics (id)
    ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS reddit_summaries (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGSERIAL,
    summary TEXT,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE reddit_summaries
    ADD CONSTRAINT fk_reddit_summaries_topics
    FOREIGN KEY (topic_id)
    REFERENCES topics (id)
    ON DELETE CASCADE;
