package boombim.domain.oauth2.application.service.impl;

import boombim.domain.oauth2.application.service.ReissueService;
import boombim.domain.user.domain.entity.Role;
import boombim.global.infra.exception.error.BoombimException;
import boombim.global.infra.exception.error.ErrorCode;
import boombim.global.jwt.domain.entity.JsonWebToken;
import boombim.global.jwt.domain.repository.JsonWebTokenRepository;
import boombim.global.jwt.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReissueServiceImpl implements ReissueService {

    private final JWTUtil jwtUtil;
    private final JsonWebTokenRepository jsonWebTokenRepository;

    @Override
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.getRefreshTokenFromCookies(request);

        if(!jwtUtil.jwtVerify(refreshToken, "refresh")) {
            log.info("Refresh token not valid");
            throw new BoombimException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        JsonWebToken jsonWebToken = jsonWebTokenRepository.findById(refreshToken).orElse(null);

        if(jsonWebToken == null) {
            throw new BoombimException(ErrorCode.REFRESH_TOKEN_NOT_EXIST);
        }

        String userId = jsonWebToken.getProviderId();
        Role role = jsonWebToken.getRole();
        String email = jsonWebToken.getEmail();

        String newAccessToken = jwtUtil.createAccessToken(userId, role, email);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, role, email);

        JsonWebToken newJsonWebToken = JsonWebToken.builder()
                .refreshToken(newRefreshToken)
                .email(email)
                .role(role)
                .build();

        jsonWebTokenRepository.delete(jsonWebToken);
        jsonWebTokenRepository.save(newJsonWebToken);

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.addHeader(HttpHeaders.COOKIE, jwtUtil.createRefreshTokenCookie(newRefreshToken).toString());
    }
}
