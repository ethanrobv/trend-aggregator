package world.erv.topics.dto;

public record TopicDto(
        Long id,
        String title,
        String wikipediaUrl,
        Long views,
        Double viewTrend
) {
}
