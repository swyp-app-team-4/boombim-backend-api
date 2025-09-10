package boombimapi.global.config;

import boombimapi.domain.favorite.application.validator.PlaceValidator;
import boombimapi.domain.place.entity.PlaceType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ValidatorConfig {

    private final List<PlaceValidator> validators;

    @Bean
    public Map<PlaceType, PlaceValidator> validatorMap() {
        Map<PlaceType, PlaceValidator> map = new HashMap<>();

        for (PlaceValidator validator : validators) {
            map.put(validator.supports(), validator);
        }

        return Collections.unmodifiableMap(map);
    }

}
