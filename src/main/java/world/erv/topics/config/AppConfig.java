package world.erv.topics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    private static final String USER_AGENT = "web:com.erv.topics:v1.0 (ethanrobv@gmail.com)";

    @Bean
    public WebClient wikipediaWebClient(WebClient.Builder builder) {
        final int bufSize = 5 * 1024 * 1024; // 5MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(bufSize))
                .build();

        return builder
                .baseUrl("https://api.wikimedia.org/feed/v1/wikipedia")
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Bean
    public WebClient redditWebClient(WebClient.Builder builder) {
        final int bufSize = 16 * 1024 * 1024; // 16MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(bufSize))
                .build();

        return builder
                .baseUrl("https://www.reddit.com")
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Bean
    public WebClient gdeltWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.gdeltproject.org/api/v2/doc/doc")
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }
}
