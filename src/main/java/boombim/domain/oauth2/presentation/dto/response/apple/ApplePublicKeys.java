package boombim.domain.oauth2.presentation.dto.response.apple;

import java.util.List;

public record ApplePublicKeys(
        List<ApplePublicKey> keys
) {}