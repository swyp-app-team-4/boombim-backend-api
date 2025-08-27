package boombimapi.domain.congestion.dto.request;

public record CreateMemberCongestionRequest(
        Long memberPlaceId,
        Integer congestionLevelId,
        String congestionMessage,
        Double latitude,
        Double longitude
) {

    public static CreateMemberCongestionRequest of(
            Long memberPlaceId,
            Integer congestionLevelId,
            String congestionMessage,
            Double latitude,
            Double longitude
    ) {
        return new CreateMemberCongestionRequest(
                memberPlaceId,
                congestionLevelId,
                congestionMessage,
                latitude,
                longitude
        );
    }
}
