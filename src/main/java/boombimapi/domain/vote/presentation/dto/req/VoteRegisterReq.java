package boombimapi.domain.vote.presentation.dto.req;

public record VoteRegisterReq(
        String posId,

        double posLatitude,

        double posLongitude,

        double userLatitude,

        double userLongitude,

        String posName

) {
}
