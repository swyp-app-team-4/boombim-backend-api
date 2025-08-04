package boombimapi.domain.oauth2.presentation.dto.response.apple;

public record ApplePublicKey(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {
}