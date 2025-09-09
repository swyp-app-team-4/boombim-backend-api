package boombimapi.domain.clova.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateCongestionMessageRequest(
    String aiAttemptToken,
    Long memberPlaceId,
    String memberPlaceName,
    String congestionLevelName,
    String congestionMessage    // optional
) {

}
