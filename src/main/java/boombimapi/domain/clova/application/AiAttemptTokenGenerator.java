package boombimapi.domain.clova.application;

import boombimapi.domain.clova.vo.AiAttemptToken;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.springframework.stereotype.Component;

@Component
public class AiAttemptTokenGenerator {

    private static final int BYTES = 16;
    private static final String PREFIX = "attempt_";

    private final SecureRandom random = new SecureRandom();
    private final Encoder base64 = Base64.getUrlEncoder().withoutPadding();

    public AiAttemptToken generateAiAttemptToken() {

        byte[] buffer = new byte[BYTES];
        random.nextBytes(buffer);

        return new AiAttemptToken(PREFIX + base64.encodeToString(buffer));
    }

}
