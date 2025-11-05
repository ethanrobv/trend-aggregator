package world.erv.topics.controller;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import world.erv.topics.dto.WikipediaArticleDto;
import world.erv.topics.model.WikipediaArticle;
import world.erv.topics.service.WikipediaService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WikipediaController {

    private final WikipediaService wikipediaService;

    @Autowired
    public WikipediaController(WikipediaService wikipediaService) {
        this.wikipediaService = wikipediaService;
    }

    @GetMapping("/article-tone-chart/{id}")
    public Mono<String> getArticleToneChart(@PathVariable Long id) {
        return wikipediaService.getArticleToneChart(id)
                .map(Json::asString);
    }

    @GetMapping("/top-articles")
    public Mono<List<WikipediaArticleDto>> getTopArticles() {

        return wikipediaService.getTopArticles(25)
                .map(this::convertToDto)
                .collectList();
    }

    private WikipediaArticleDto convertToDto(WikipediaArticle article) {
        return new WikipediaArticleDto(
                article.getId(),
                article.getTitle(),
                article.getViews(),
                article.getUrl(),
                article.getViewTrend()
        );
    }
}
