package boombimapi.global.config;

import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.domain.place.cluster.impl.GridClusterer;
import boombimapi.global.properties.AppClusterProperties;
import boombimapi.global.properties.WebClusterProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WebClusterProperties.class, AppClusterProperties.class})
public class ClusterConfig {

    @Bean
    public Clusterer webClusterer(
        WebClusterProperties properties
    ) {
        return new GridClusterer(properties);
    }

    @Bean
    public Clusterer appClusterer(
        AppClusterProperties properties
    ) {
        return new GridClusterer(properties);
    }

}
