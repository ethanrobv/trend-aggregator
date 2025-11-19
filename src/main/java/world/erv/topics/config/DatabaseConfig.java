package world.erv.topics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
// For @CreatedDate
@EnableR2dbcAuditing
public class DatabaseConfig {
}
