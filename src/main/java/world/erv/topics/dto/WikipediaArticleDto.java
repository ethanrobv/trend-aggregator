package world.erv.topics.dto;

public record WikipediaArticleDto(
        Long id,
        String title,
        long views,
        String articleUrl,
        double viewTrend) {
}
