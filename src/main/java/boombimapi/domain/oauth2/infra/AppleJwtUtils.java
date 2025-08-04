package boombimapi.domain.oauth2.infra;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
@Component
@Slf4j
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
            PrivateKey pKey = getPrivateKey();

            return Jwts.builder()
                    .setHeaderParam("kid", keyId)
                    .setHeaderParam("alg", "ES256")
                    .setIssuer(teamId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1일
                    .setAudience("https://appleid.apple.com")
                    .setSubject(clientId)
                    .signWith(pKey, SignatureAlgorithm.ES256)
                    .compact();
        } catch (Exception e) {
            log.error("Apple JWT 생성 실패", e);
            throw new BoombimException(ErrorCode.APPLE_JWT_ERROR);
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        try {
            // PEM 형식의 private key에서 헤더/푸터 제거 및 개행 문자 제거
            String privateKeyPEM = privateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);

            // PKCS8 형식으로 키 생성
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Apple Private Key 파싱 실패", e);
            throw new Exception("Private key parsing failed", e);
        }
    }
}