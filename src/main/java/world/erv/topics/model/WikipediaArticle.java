package world.erv.topics.model;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("wikipedia_articles")
public class WikipediaArticle {

    @Id
    private Long id;
    private String title;
    private long views;
    private String url;

    @CreatedDate
    private Instant createdAt;

    @Column("gdelt_tone_chart")
    private Json toneChart;

    @Column("view_trend")
    private double viewTrend;

    public WikipediaArticle() {
    }

    public WikipediaArticle(String title, long views, String url, double viewTrend) {
        this.title = title;
        this.views = views;
        this.url = url;
        this.viewTrend = viewTrend;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Json getToneChart() { return toneChart; }
    public void setToneChart(Json toneChart) { this.toneChart = toneChart; }

    public double getViewTrend() { return viewTrend; }
    public void setViewTrend(double viewTrend) { this.viewTrend = viewTrend; }
}
