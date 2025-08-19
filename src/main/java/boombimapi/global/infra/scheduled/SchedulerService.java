package boombimapi.global.infra.scheduled;

import boombimapi.domain.alarm.application.service.AlarmService;
import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmType;
import boombimapi.domain.alarm.presentation.dto.req.SendAlarmRequest;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchedulerService {
    private final FcmService fcmService;
    private final VoteRepository voteRepository;
    private final VoteDuplicationRepository voteDuplicationRepository;
    private final VoteAnswerRepository voteAnswerRepository;
    private final AlarmService alarmService;
    private final MessageService messageService;

    @Value("${admin.id}")
    private String adminId;


    // 매일 새벽 3시에 오래된 FCM 토큰 정리
    @Scheduled(cron = "0 0 3 * * *") // 매일 오전 3시
    public void cleanupOldFcmTokens() {
        log.info("오래된 FCM 토큰 정리 작업 시작");
        try {
            fcmService.cleanupOldTokens();
            log.info("FCM 토큰 정리 작업 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 정리 작업 중 오류 발생", e);
        }
    }

    // 투표 종류 후 알림
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L) // 1분마다, 앱 시작 30초 후 시작
    public void sweepExpiredVotes() {
        // 자동
        auto();

        // 수동
        passivity();
    }


    // 매일 오후 4시에 소통방 알림
    @Scheduled(cron = "0 0 16 * * *") // 매일 오후 4시
    public void sendDailyNotification() {
        log.info("오후 4시 알림 작업 시작");
        try {
            // 관리자 아이디 나중에 바꾸게씅.!
            alarmService.sendAllAlarm(adminId, SendAlarmRequest
                    .builder()
                    .title(messageService.dailyCommunityTitle())
                    .message(messageService.dailyCommunityMessage())
                    .type(AlarmType.COMMUNICATION).build());
            log.info("오후 4시 알림 작업 완료");
        } catch (Exception e) {
            log.error("오후 4시 알림 작업 중 오류 발생", e);
        }
    }


    private void auto() {
        List<Vote> autoVotes = voteRepository.findByVoteStatusAndEndTimeLessThanEqual(VoteStatus.PROGRESS, LocalDateTime.now());

        // 투포 시간 된거 종료로 바꾸기
        voteRepository.bulkCloseExpired(VoteStatus.PROGRESS, VoteStatus.END, LocalDateTime.now());
        // log.debug("Closed {} expired votes", n);

        // 자동 종료 알림
        for (Vote autoVote : autoVotes) {
            List<Member> baseMemberList = getBaseMembers(autoVote);
            alarmService.sendEndVoteAlarm(autoVote, baseMemberList, true);

            List<Member> answerMemberList = getAnswerersOnly(autoVote);
            alarmService.sendEndVoteAlarm(autoVote, answerMemberList, false);

        }
    }

    private void passivity() {
        List<Vote> passivityVotes = voteRepository.findByPassivityAlarmFlagTrue();

        // 수동 종료 알림
        for (Vote passivityVote : passivityVotes) {
            // false로 전환
            passivityVote.updatePassivityAlarmDeactivate();

            // 한번더 안전상으로 투표 종료 비활성화 false로 바꿈
            passivityVote.updateIsVoteDeactivate();
            passivityVote.updateStatusDeactivate();

            List<Member> baseMemberList = getBaseMembers(passivityVote);
            if (!baseMemberList.isEmpty()) alarmService.sendEndVoteAlarm(passivityVote, baseMemberList, true);

            List<Member> answerMemberList = getAnswerersOnly(passivityVote);
            if (!answerMemberList.isEmpty()) alarmService.sendEndVoteAlarm(passivityVote, answerMemberList, false);
        }
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


    private List<Member> getMembers(Vote vote) {
        // 종료 알람 넣기
        Set<Member> memberSet = new HashSet<>();

        // 1) 투표 생성자
        memberSet.addAll(voteRepository.findMembersByVote(vote));

        // 2) 중복투표한 유저
        memberSet.addAll(voteDuplicationRepository.findMembersByVote(vote));

        // 3) 답변한 유저
        memberSet.addAll(voteAnswerRepository.findMembersByVote(vote));

        // 최종 리스트
        List<Member> memberList = new ArrayList<>(memberSet);
        return memberList;
    }

}
