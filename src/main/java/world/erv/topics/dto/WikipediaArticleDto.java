package world.erv.topics.dto;

public record WikipediaArticleDto(
        String title,
        String url,
        long views,
        double viewTrend
) {
}
