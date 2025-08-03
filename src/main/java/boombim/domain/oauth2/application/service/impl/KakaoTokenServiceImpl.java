package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.KakaoAccessTokenAndRefreshTokenService;
import boombim.domain.oauth2.application.service.KakaoTokenService;
import boombim.domain.oauth2.presentation.dto.response.oatuh.OAuth2TokenResponse;
import boombim.global.jwt.domain.entity.KakaoJsonWebToken;
import boombim.global.jwt.domain.repository.KakaoJsonWebTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KakaoTokenServiceImpl implements KakaoTokenService {
    private final KakaoJsonWebTokenRepository KakaoTokenRepository;
    private final KakaoAccessTokenAndRefreshTokenService tokenService;

    @Override
    public String getValidAccessToken(String userId) {
        KakaoJsonWebToken token = KakaoTokenRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Kakao 토큰을 찾을 수 없습니다."));

        if (token.getExpiresIn() == null || token.getExpiresIn().minusMinutes(10).isBefore(LocalDateTime.now())) {
            OAuth2TokenResponse newToken = tokenService.refreshAccessToken(token.getRefreshToken());

            // 새 토큰 저장
            KakaoJsonWebToken updatedToken = KakaoJsonWebToken.builder()
                    .userId(userId)
                    .accessToken(newToken.accessToken())
                    .refreshToken(token.getRefreshToken()) // refresh token은 유지
                    .expiresIn(LocalDateTime.now().plusHours(1))
                    .build();
            KakaoTokenRepository.save(updatedToken);

            log.info("리프레쉬 토큰 재발급");

            return "Bearer " + newToken.accessToken();
        }
        return "Bearer " + token.getAccessToken();
    }
}
