package boombimapi.domain.oauth2.presentation.dto.res.apple;

import java.util.List;

public record ApplePublicKeys(
        List<ApplePublicKey> keys
) {
}