package world.erv.topics.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("wikipedia_view_history")
public class WikipediaViewHistory {

    @Id
    private Long id;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant modifiedAt;
    @Column("topic_id")
    private Long topicId;
    @Column("view_trend")
    private Double viewTrend;
    private Long views;

    public WikipediaViewHistory() {
    }

    public WikipediaViewHistory(
            Long topicId,
            Double viewTrend,
            Long views
    ) {
        this.topicId = topicId;
        this.viewTrend = viewTrend;
        this.views = views;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Double getViewTrend() {
        return viewTrend;
    }

    public void setViewTrend(Double viewTrend) {
        this.viewTrend = viewTrend;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }
}
