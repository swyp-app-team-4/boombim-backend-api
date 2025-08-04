package boombimapi.domain.oauth2.application.service.impl.auth;

import boombimapi.domain.oauth2.application.service.LogoutService;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.jwt.domain.entity.JsonWebToken;
import boombimapi.global.jwt.domain.repository.JsonWebTokenRepository;
import boombimapi.global.jwt.domain.repository.SocialTokenRepository;
import boombimapi.global.jwt.util.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LogoutServiceImpl implements LogoutService {

    private final JWTUtil jwtUtil;
    private final JsonWebTokenRepository jsonWebTokenRepository;
    private final SocialTokenRepository socialTokenRepository;

    @Override
    public void logout(String refreshToken) {
        if(!jwtUtil.jwtVerify(refreshToken, "refresh")) {
            throw new BoombimException(ErrorCode.JWT_ERROR_TOKEN);
        }

        JsonWebToken jsonWebToken = jsonWebTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new BoombimException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        // 사용자의 모든 소셜 토큰 삭제
        socialTokenRepository.deleteByUserId(jsonWebToken.getProviderId());
        jsonWebTokenRepository.delete(jsonWebToken);

        log.info("사용자 로그아웃 완료: userId={}", jsonWebToken.getProviderId());
    }
}