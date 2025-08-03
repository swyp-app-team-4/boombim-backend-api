package boombim.domain.oauth2.infra;


import boombim.global.infra.exception.error.BoombimException;
import com.nimbusds.jose.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.nimbusds.oauth2.sdk.ResponseMode.JWT;

@Component
public class AppleJwtUtils {

    @Value("${oauth2.apple.team-id}")
    private String teamId;

    @Value("${oauth2.apple.key-id}")
    private String keyId;

    @Value("${oauth2.apple.private-key}")
    private String privateKey;

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    public String generateClientSecret() {
        try {
            // Apple의 private key로 JWT 생성
            Algorithm algorithm = Algorithm.ECDSA256(null, getPrivateKey());

            return JWT.create()
                    .withIssuer(teamId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 1일
                    .withAudience("https://appleid.apple.com")
                    .withSubject(clientId)
                    .withKeyId(keyId)
                    .sign(algorithm);
        } catch (Exception e) {
            throw new BoombimException(ErrorCode.APPLE_JWT_ERROR);
        }
    }

    private ECPrivateKey getPrivateKey() throws Exception {
        // privateKey 문자열을 ECPrivateKey로 변환하는 로직
        // Base64 디코딩 및 키 파싱 구현
    }
}