package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.CreateAccessTokenAndRefreshTokenService;
import boombim.domain.user.domain.entity.Role;
import boombim.global.jwt.domain.entity.JsonWebToken;
import boombim.global.jwt.domain.repository.JsonWebTokenRepository;
import boombim.global.jwt.util.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateAccessTokenAndRefreshTokenServiceImpl implements CreateAccessTokenAndRefreshTokenService {

    private final JWTUtil jwtUtil;
    private final JsonWebTokenRepository jsonWebTokenRepository;

    @Override
    public Map<String, String> createAccessTokenAndRefreshToken(String userId, Role role, String email) {
        String accessToken = jwtUtil.createAccessToken(userId, role, email);
        String refreshToken = jwtUtil.createRefreshToken(userId, role, email);

        JsonWebToken jsonWebToken = JsonWebToken.builder()
                .refreshToken(refreshToken)
                .providerId(userId)
                .role(role)
                .email(email)
                .build();

        jsonWebTokenRepository.save(jsonWebToken);

        String refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken).toString();

        return Map.of("access_token", accessToken, "refresh_token_cookie", refreshTokenCookie);
    }
}
