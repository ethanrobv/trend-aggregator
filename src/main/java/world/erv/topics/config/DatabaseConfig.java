package world.erv.topics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
// For @CreatedDate
@EnableR2dbcAuditing
// For @Transactional
@EnableTransactionManagement
public class DatabaseConfig {
}
