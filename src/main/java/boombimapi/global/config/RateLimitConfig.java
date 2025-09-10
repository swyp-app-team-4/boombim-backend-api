package boombimapi.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RateLimitConfig {

    @Bean
    public DefaultRedisScript<String> aiAttemptTokenBucketScript(
        @Value("classpath:lua/ai_attempt_token_bucket.lua") Resource resource
    ) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(resource));
        script.setResultType(String.class);

        return script;
    }

}
