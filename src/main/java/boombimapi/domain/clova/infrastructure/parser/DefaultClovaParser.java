package boombimapi.domain.clova.infrastructure.parser;

import boombimapi.domain.clova.dto.response.ClovaResponse;
import org.springframework.stereotype.Component;

@Component
public class DefaultClovaParser implements ClovaParser {

    @Override
    public String toText(
        ClovaResponse clovaResponse
    ) {

        return clovaResponse.result().message().content();
    }
}
