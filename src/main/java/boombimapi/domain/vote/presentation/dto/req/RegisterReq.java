package boombimapi.domain.vote.presentation.dto.req;

public record RegisterReq(
        String posId,

        double posLatitude,

        double posLongitude,

        double userLatitude,

        double userLongitude,

        String posName

) {
}
