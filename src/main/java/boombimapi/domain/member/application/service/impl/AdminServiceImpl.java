package boombimapi.domain.member.application.service.impl;

import boombimapi.domain.member.application.service.AdminService;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.admin.req.AdminLoginReq;
import boombimapi.domain.oauth2.application.service.CreateAccessTokenAndRefreshTokenService;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CreateAccessTokenAndRefreshTokenService tokenService; // 기존 OAuth2 토큰 서비스 재사용

    @Override
    public LoginToken postLogin(AdminLoginReq req) {
        // 이메일로 사용자 찾기
        Member member = memberRepository.findByEmail(req.loginId()).orElse(null);
        if (member == null) {
            log.error("로그인 실패: 존재하지 않는 이메일 - {}", req.loginId());
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);
        }


        // 비밀번호 확인
        if (member.getPassword() == null || !passwordEncoder.matches(req.password(), member.getPassword())) {
            log.error("로그인 실패: 비밀번호 불일치 - {}", req.loginId());
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);
        }

        log.info("일반 로그인 성공: userId={}, email={}, role={}", member.getId(), member.getEmail(), member.getRole());

        // 기존 OAuth2 토큰 생성 서비스를 재사용하여 JWT 토큰 생성
        return tokenService.createAccessTokenAndRefreshToken(
                member.getId(),
                member.getRole(),
                member.getEmail()
        );
    }
}