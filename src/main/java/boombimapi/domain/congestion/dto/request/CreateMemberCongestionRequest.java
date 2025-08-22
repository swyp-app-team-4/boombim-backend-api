package boombimapi.domain.congestion.dto.request;

public record CreateMemberCongestionRequest(
    Long memberPlaceId,
    Integer congestionLevelId,
    String congestionMessage,
    Double latitude,
    Double longitude
) {

}
