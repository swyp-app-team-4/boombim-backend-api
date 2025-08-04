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
import java.util.HashMap;
import java.util.Map;

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
            long now = System.currentTimeMillis();

            Map<String, Object> header = new HashMap<>();
            header.put("kid", keyId);
            header.put("alg", "ES256");

            return Jwts.builder()
                    .setHeader(header)
                    .setIssuer(teamId)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(now + 86400000)) // 24시간
                    .setAudience("https://appleid.apple.com")
                    .setSubject(clientId)
                    .signWith(pKey, SignatureAlgorithm.ES256)
                    .compact();
        } catch (Exception e) {
            log.error("Apple JWT 생성 실패: {}", e.getMessage(), e);
            throw new BoombimException(ErrorCode.APPLE_JWT_ERROR);
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        try {
            log.debug("Private Key 처리 시작");

            // 환경변수에서 받은 키 값 로깅 (보안상 일부만)
            log.debug("Private Key 길이: {}", privateKey != null ? privateKey.length() : 0);

            // PEM 형식의 private key에서 헤더/푸터 제거 및 개행 문자 처리
            String privateKeyPEM = privateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\n", "") // 환경변수의 \n 문자열을 제거
                    .replaceAll("\\s", ""); // 모든 공백 문자 제거

            log.debug("처리된 Private Key 길이: {}", privateKeyPEM.length());

            // Base64 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            log.debug("디코딩된 키 바이트 길이: {}", keyBytes.length);

            // PKCS8 형식으로 키 생성
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            PrivateKey key = keyFactory.generatePrivate(spec);
            log.debug("Private Key 생성 성공: {}", key.getAlgorithm());

            return key;
        } catch (Exception e) {
            log.error("Apple Private Key 파싱 실패: {}", e.getMessage(), e);
            throw new Exception("Private key parsing failed: " + e.getMessage(), e);
        }
    }
}