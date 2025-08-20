package boombimapi.global.infra.init;

import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.entity.Role;
import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.alarm.domain.repository.FcmTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberDataInitializer {

    private final MemberRepository memberRepository;
    private final FcmTokenRepository fcmTokenRepository;

    private static final String FCM_TOKEN = "e5y6VivpfCYAjx2g8r5-Kx:APA91bGfCtdsZ7jqYn7G-U8lLICS4QvzA6Lp__AdSnyAOEZe99icXeNlMQFfxapLNeUVW1J-agbBCj36OBWzyXG7c_UihrOQZLLEGFdGXSo1qPoyfO-r8hU";

    @PostConstruct
    @Transactional
    public void initializeMembers() {
        // 이미 데이터가 있으면 초기화하지 않음
        if (memberRepository.count() > 0) {
            log.info("회원 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("회원 데이터 초기화를 시작합니다... (100명)");

        for (int i = 1; i <= 100; i++) {
            // Member 생성
            Member member = Member.builder()
                    .id(UUID.randomUUID().toString())
                    .email("test" + i + "@kakao.com")
                    .name("테스트유저" + i)
                    .profile("https://example.com/profile" + i + ".jpg")
                    .socialProvider(SocialProvider.KAKAO)
                    .role(Role.USER)
                    .build();

            Member savedMember = memberRepository.save(member);

            // FCM 토큰 생성
            FcmToken fcmToken = FcmToken.builder()
                    .member(savedMember)
                    .token(FCM_TOKEN)
                    .deviceType(DeviceType.ANDROID)
                    .build();

            fcmTokenRepository.save(fcmToken);

            if (i % 10 == 0) {
                log.info("회원 {}명 초기화 완료", i);
            }
        }

        log.info("회원 데이터 초기화가 완료되었습니다. 총 100명의 회원과 FCM 토큰이 생성되었습니다.");
    }
}