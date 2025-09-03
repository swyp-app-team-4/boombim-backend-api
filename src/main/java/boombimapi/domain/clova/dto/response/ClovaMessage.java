package boombimapi.domain.clova.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClovaMessage(
    String content
) {

}
