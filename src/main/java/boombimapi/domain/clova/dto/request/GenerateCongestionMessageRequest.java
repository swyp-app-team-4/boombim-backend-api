package boombimapi.domain.clova.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateCongestionMessageRequest(
    String memberPlaceName,
    String congestionLevelName,
    String congestionMessage    // optional
) {

}
