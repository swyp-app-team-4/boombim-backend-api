package boombimapi.domain.clova.dto.response;

public record GenerateCongestionMessageResponse(
    String generatedCongestionMessage
) {

    public static GenerateCongestionMessageResponse from(
        String result
    ) {
        return new GenerateCongestionMessageResponse(result);
    }

}
