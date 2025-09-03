package boombimapi.domain.clova.infrastructure.parser;

import boombimapi.domain.clova.dto.response.ClovaResponse;

public interface ClovaParser {

    String toText(
        ClovaResponse clovaResponse
    );
}
