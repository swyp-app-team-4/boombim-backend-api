package boombimapi.domain.alarm.application.consumer;

import boombimapi.domain.alarm.application.messaging.EndVoteMessage;
import boombimapi.domain.alarm.application.service.impl.FcmSender;
import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.alarm.domain.repository.AlarmRecipientRepository;
import boombimapi.domain.alarm.domain.repository.AlarmRepository;
import boombimapi.domain.alarm.domain.repository.FcmTokenRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.global.config.RabbitMQConfig;

import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndVoteWorker {

    private final VoteRepository voteRepository;
    private final MemberRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmRecipientRepository alarmRecipientRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmSender fcmSender;
    private final VoteDuplicationRepository voteDuplicationRepository;
    private final VoteAnswerRepository voteAnswerRepository;

    private static final int BATCH_SIZE = 500;

    @Value("${admin.id}")
    private String adminId;
    @RabbitListener(queues = RabbitMQConfig.Q_END_VOTE, concurrency = "3-5")
    public void onEndVote(EndVoteMessage msg) {
        log.info("투표 종료 알림 처리 시작: voteId={}, isQuestioner={}", msg.getVoteId(), msg.isQuestioner());

        Vote vote = voteRepository.findById(msg.getVoteId())
                .orElseThrow(() -> new IllegalStateException("Vote not found"));

        Member sender = userRepository.findById(adminId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        String title = msg.isQuestioner()
                ? "[투표 종료] 질문자 알림"
                : "[투표 종료] 투표자 알림";
        String body = msg.isQuestioner()
                ? "당신이 만든 투표가 종료되었습니다."
                : "참여한 투표가 종료되었습니다.";

        Alarm alarm = alarmRepository.save(Alarm.builder()
                .title(title)
                .message(body)
                .type(AlarmType.VOTE)
                .sender(sender)
                .build());

        alarm.updateStatus(AlarmStatus.SENDING);

        List<Member> targets = msg.isQuestioner()
                ? getBaseMembers(vote)
                : getAnswerersOnly(vote);

        if (targets.isEmpty()) {
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findActiveTokensForUsers(targets);
        if (tokens.isEmpty()) {
            return;
        }

        log.info("투표 종료 알림 전송 시작: voteId={}, 총 대상자={}, 유효 토큰={}",
                vote.getId(), targets.size(), tokens.size());

        int success = 0, failure = 0;
        List<String> invalidTokens = new ArrayList<>();
        List<AlarmRecipient> recipients = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i += BATCH_SIZE) {
            List<FcmToken> batch = tokens.subList(i, Math.min(i + BATCH_SIZE, tokens.size()));
            log.info("=== FCM 배치 전송 시작 (voteId={}, batchStart={}, batchEnd={}) ===",
                    vote.getId(), i, i + batch.size());

            var result = fcmSender.sendBatchNotification(batch, title, body);

            log.info("배치 전송 결과 (voteId={}): 성공={}, 실패={}, 무효토큰={}",
                    vote.getId(), result.successCount(), result.failureCount(), result.invalidTokens().size());

            success += result.successCount();
            failure += result.failureCount();
            invalidTokens.addAll(result.invalidTokens());

            Set<String> invalidSet = new HashSet<>(result.invalidTokens());
            for (FcmToken ft : batch) {
                AlarmRecipient ar = AlarmRecipient.builder()
                        .alarm(alarm)
                        .member(ft.getMember())
                        .deviceType(ft.getDeviceType())
                        .build();
                if (invalidSet.contains(ft.getToken())) {
                    ar.markFailed("INVALID_OR_UNREGISTERED");
                } else {
                    ar.markSent();
                }
                recipients.add(ar);
            }
        }

        alarmRecipientRepository.saveAll(recipients);
        deactivateInvalidTokens(invalidTokens);

        if (success > 0) {
            alarm.updateStatus(AlarmStatus.SENT);
        } else {
            alarm.updateFailureReason("모든 대상자에게 전송 실패");
        }

        log.info("투표 종료 알림 처리 완료: alarmId={}, voteId={}, 성공={}, 실패={}, 무효토큰={}",
                alarm.getId(), vote.getId(), success, failure, invalidTokens.size());
    }


    private void deactivateInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            fcmTokenRepository.findByToken(token).ifPresent(FcmToken::deactivate);
        }
        log.info("무효 토큰 {}개 비활성화 완료", invalidTokens.size());
    }

    private List<Member> getBaseMembers(Vote vote) {
        // 1) 투표 생성자
        List<Member> creators = voteRepository.findMembersByVote(vote);

        // 2) 중복투표한 유저
        List<Member> duplicators = voteDuplicationRepository.findMembersByVote(vote);

        // 1+2 합치고 중복 제거 (id 기준)
        Map<String, Member> byId = new LinkedHashMap<>();
        for (Member m : creators) byId.put(m.getId(), m);
        for (Member m : duplicators) byId.put(m.getId(), m);

        return new ArrayList<>(byId.values());
    }

    private List<Member> getAnswerersOnly(Vote vote) {
        // 1+2 베이스 멤버의 id 세트
        List<Member> baseMembers = getBaseMembers(vote);
        Set<String> baseIds = baseMembers.stream()
                .map(Member::getId)
                .collect(Collectors.toSet());

        // 3) 답변자 불러와서, 1+2에 없는 사람만 필터
        List<Member> answerers = voteAnswerRepository.findMembersByVote(vote);
        return answerers.stream()
                .filter(m -> !baseIds.contains(m.getId()))
                .distinct() // 혹시 쿼리 중복 대비
                .collect(Collectors.toList());
    }
}
