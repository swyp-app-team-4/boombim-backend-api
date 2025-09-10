package boombimapi.domain.oauth2.application.service.impl;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.oauth2.application.service.CreateAccessTokenAndRefreshTokenService;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.domain.member.domain.entity.Role;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.jwt.domain.entity.JsonWebToken;
import boombimapi.global.jwt.domain.repository.JsonWebTokenRepository;
import boombimapi.global.jwt.util.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateAccessTokenAndRefreshTokenServiceImpl implements CreateAccessTokenAndRefreshTokenService {

    private final JWTUtil jwtUtil;
    private final JsonWebTokenRepository jsonWebTokenRepository;
    private final MemberRepository memberRepository;

    @Override
    public LoginToken createAccessTokenAndRefreshToken(String userId, Role role, String email) {
        String accessToken = jwtUtil.createAccessToken(userId, role, email);
        String refreshToken = jwtUtil.createRefreshToken(userId, role, email);

        JsonWebToken jsonWebToken = JsonWebToken.builder()
                .refreshToken(refreshToken)
                .providerId(userId)
                .role(role)
                .email(email)
                .build();

        jsonWebTokenRepository.save(jsonWebToken);

        boolean nameFlag = onBoarding(userId);
        return LoginToken.of(accessToken, refreshToken, nameFlag);
    }

    public boolean onBoarding(String userId) {
        Member member = memberRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        return member.isNameFlag();
    }
}