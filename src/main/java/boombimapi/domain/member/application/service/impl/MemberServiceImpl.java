package boombimapi.domain.member.application.service.impl;


import boombimapi.domain.member.application.service.MemberService;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.entity.MemberLeave;
import boombimapi.domain.member.domain.repository.MemberLeaveRepository;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.member.req.MemberLeaveReq;
import boombimapi.domain.member.presentation.dto.member.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.member.res.GetNicknameRes;
import boombimapi.domain.member.presentation.dto.member.res.MyPageVoteRes;
import boombimapi.domain.member.presentation.dto.member.res.ProfileRes;
import boombimapi.domain.member.presentation.dto.member.res.mypage.MPVoteRes;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.s3.presentation.application.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository userRepository;
    private final VoteAnswerRepository voteAnswerRepository;
    private final VoteDuplicationRepository voteDuplicationRepository;
    private final VoteRepository voteRepository;
    private final S3Service s3Service;
    private final MemberLeaveRepository memberLeaveRepository;

    @Override
    public GetMemberRes getMember(String userId) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        // 투표수
        List<VoteAnswer> voteAnswers = voteAnswerRepository.findByMember(user);

        //질문수
        List<VoteDuplication> voteDus = voteDuplicationRepository.findByMember(user);
        List<Vote> votes = voteRepository.findByMember(user);

        return GetMemberRes.of(user, (long) voteAnswers.size(), (long) (voteDus.size() + votes.size()));
    }

    // 3번 api 투표
    @Override
    public List<MyPageVoteRes> getMyVoteAnswer(String userId) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        // 1. 하위 데이터 먼저 수집
        List<MPVoteRes> bottomResult = new ArrayList<>();

        List<VoteAnswer> voteAnswers = voteAnswerRepository.findByMember(member);

        for (VoteAnswer voteAnswer : voteAnswers) {
            Vote vote = voteAnswer.getVote();

            // 장소 이름
            String posName = vote.getPosName();

            // 인기 투표 타입과 투표수
            List<VoteAnswerType> voteAnswerTypes = popularTypes(vote);

            // 각각의 투표 수들 파악
            List<Long> vote4Answer = voteAnswerCnt(vote);

            // 총 투표수
            Long voteALlCnt = vote4Answer.get(0) + vote4Answer.get(1) + vote4Answer.get(2) + vote4Answer.get(3);

            // 상위 3건 유저 프로필 사진
            List<String> profile = profileTopThree(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), profile, vote.getCreatedAt(), posName, voteAnswerTypes,
                    vote4Answer.get(0), vote4Answer.get(1), vote4Answer.get(2), vote4Answer.get(3), voteALlCnt, vote.getVoteStatus()));
        }

        if (bottomResult.isEmpty()) {
            return List.of();
        }

        return dayMapping(bottomResult);
    }


    // 4번 api 질문
    @Override
    public List<MyPageVoteRes> getMyVoteQuestion(String userId) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        // 1. 하위 데이터 수집
        List<MPVoteRes> bottomResult = new ArrayList<>();
        List<Vote> votes = voteRepository.findByMember(member);
        List<VoteDuplication> voteDus = voteDuplicationRepository.findByMember(member);

        // 본인이 올린거
        for (Vote vote : votes) {
            // 장소 이름
            String posName = vote.getPosName();

            // 인기 투표 타입과 투표수
            List<VoteAnswerType> voteAnswerTypes = popularTypes(vote);

            // 각각의 투표 수들 파악
            List<Long> vote4Answer = voteAnswerCnt(vote);

            // 총 투표수
            Long voteALlCnt = vote4Answer.get(0) + vote4Answer.get(1) + vote4Answer.get(2) + vote4Answer.get(3);

            // 상위 3건 유저 프로필 사진
            List<String> profile = profileTopThree(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), profile, vote.getCreatedAt(), posName, voteAnswerTypes,
                    vote4Answer.get(0), vote4Answer.get(1), vote4Answer.get(2), vote4Answer.get(3), voteALlCnt, vote.getVoteStatus()));

        }

        // 투표 중복 올린거
        for (VoteDuplication voteDp : voteDus) {
            Vote vote = voteDp.getVote();

            // 장소 이름
            String posName = vote.getPosName();

            // 인기 투표 타입과 투표수
            List<VoteAnswerType> voteAnswerTypes = popularTypes(vote);

            // 각각의 투표 수들 파악
            List<Long> vote4Answer = voteAnswerCnt(vote);

            // 총 투표수
            Long voteALlCnt = vote4Answer.get(0) + vote4Answer.get(1) + vote4Answer.get(2) + vote4Answer.get(3);

            // 상위 3건 유저 프로필 사진
            List<String> profile = profileTopThree(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), profile, vote.getCreatedAt(), posName, voteAnswerTypes,
                    vote4Answer.get(0), vote4Answer.get(1), vote4Answer.get(2), vote4Answer.get(3), voteALlCnt, vote.getVoteStatus()));
        }


        if (bottomResult.isEmpty()) {
            return List.of();
        }

        return dayMapping(bottomResult);


    }


    @Override
    public void updateNickname(String userId, String name) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        member.updateName(name);
    }

    @Override
    public GetNicknameRes getNameFlag(String userId) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        return GetNicknameRes.of(member.isNameFlag());
    }

    @Override
    public void memberDelete(String userId, MemberLeaveReq req) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        memberLeaveRepository.save(MemberLeave.builder().leaveReason(req.leaveReason()).build());

        userRepository.delete(member);
    }

    @Override
    public ProfileRes updateProfile(String userId, MultipartFile multipartFile) throws IOException {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);


        String profile = s3Service.storeUserProFile(multipartFile, userId);
        member.updateProfile(profile);
        return ProfileRes.of(profile);
    }

    // 인기 투표 타입 리스트 반환 (동점 허용)
    public List<VoteAnswerType> popularTypes(Vote vote) {
        List<VoteAnswer> voteAnswers = vote.getVoteAnswers();

        if (voteAnswers == null || voteAnswers.isEmpty()) {
            return Collections.emptyList(); // 없을 때는 빈 리스트 반환
        }

        // 타입별 카운팅
        Map<VoteAnswerType, Long> countMap = voteAnswers.stream()
                .collect(Collectors.groupingBy(VoteAnswer::getAnswerType, Collectors.counting()));

        // 최대 투표 수 찾기
        long maxCount = countMap.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        if (maxCount == 0L) {
            return Collections.emptyList();
        }

        // 최대값과 같은 타입들만 추출
        return countMap.entrySet().stream()
                .filter(e -> e.getValue().equals(maxCount))
                .map(Map.Entry::getKey)
                .toList();
    }


    // 인기 투표 즉 값 많은거 찾기
    public Map.Entry<String, Long> popularCnt(Vote vote) {
        List<VoteAnswer> voteAnswers = vote.getVoteAnswers();

        if (voteAnswers == null || voteAnswers.isEmpty()) {
            return Map.entry("없음", 0L);
        }

        Map<VoteAnswerType, Long> countMap = voteAnswers.stream()
                .collect(Collectors.groupingBy(VoteAnswer::getAnswerType, Collectors.counting()));

        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> Map.entry(e.getKey().name(), e.getValue()))
                .orElse(Map.entry("없음", 0L));
    }


    // 날짜 매핑
    private List<MyPageVoteRes> dayMapping(List<MPVoteRes> bottomResult) {
        // 2) 날짜(일 단위)로 그룹핑
        Map<LocalDate, List<MPVoteRes>> grouped = bottomResult.stream()
                .collect(Collectors.groupingBy(r -> r.day().toLocalDate()));

        // 3) 날짜 내림차순(최근일자 먼저),
        //    같은 날짜 내에서는 시간 내림차순으로 정렬해서 MyPageVoteRes 구성
        List<MyPageVoteRes> result = grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<MPVoteRes>>comparingByKey(Comparator.reverseOrder()))
                .map(e -> {
                    List<MPVoteRes> items = e.getValue().stream()
                            .sorted(Comparator.comparing(MPVoteRes::day).reversed())
                            .toList();
                    LocalDateTime headerDay = e.getKey().atStartOfDay(); // 날짜 헤더(00:00)로 표시
                    return MyPageVoteRes.of(headerDay, items);
                })
                .toList();
        return result;
    }

    // 투표마다 투표 4개 답변 숫 얻어오기
    public List<Long> voteAnswerCnt(Vote vote) {
        List<VoteAnswer> voteAnswer = voteAnswerRepository.findByVote(vote);
        Map<VoteAnswerType, Long> counts = voteAnswer.stream()
                .collect(Collectors.groupingBy(VoteAnswer::getAnswerType, Collectors.counting()));

        long relaxedCount = counts.getOrDefault(VoteAnswerType.RELAXED, 0L);
        long commonlyCount = counts.getOrDefault(VoteAnswerType.COMMONLY, 0L);
        long slightlyBusyCount = counts.getOrDefault(VoteAnswerType.BUSY, 0L);
        long crowdedCount = counts.getOrDefault(VoteAnswerType.CROWDED, 0L);

        List<Long> result = new ArrayList<>();
        result.add(relaxedCount);
        result.add(commonlyCount);
        result.add(slightlyBusyCount);
        result.add(crowdedCount);
        return result;
    }

    // 상위 3건 유저 프로필 이미지 링크
    public List<String> profileTopThree(Vote vote) {
        return vote.getVoteAnswers().stream()
                .map(voteAnswer -> voteAnswer.getMember().getProfile()) // Member의 프로필 URL 추출
                .filter(Objects::nonNull)                               // null 값 제거 (안전)
                .limit(3)                                               // 최대 3개만
                .toList();
    }


}
