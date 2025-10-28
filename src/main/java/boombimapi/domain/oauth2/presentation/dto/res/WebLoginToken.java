package boombimapi.domain.oauth2.presentation.dto.res;

public record WebLoginToken(
    String accessToken
) {

    public static WebLoginToken from(
        String accessToken
    ) {
        return new WebLoginToken(
            accessToken
        );
    }

}
