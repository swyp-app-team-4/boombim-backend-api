package boombimapi.domain.member.application.service.impl;


import boombimapi.domain.member.application.service.MemberService;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.member.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.member.res.GetNicknameRes;
import boombimapi.domain.member.presentation.dto.member.res.MyPageVoteRes;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            Map.Entry<String, Long> answerTypeAndCnt = popularCnt(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), vote.getCreatedAt(), posName, answerTypeAndCnt.getKey(),
                    answerTypeAndCnt.getValue()));
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
            Map.Entry<String, Long> answerTypeAndCnt = popularCnt(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), vote.getCreatedAt(), posName, answerTypeAndCnt.getKey(),
                    answerTypeAndCnt.getValue()));
        }

        // 투표 중복 올린거
        for (VoteDuplication voteDp : voteDus) {
            Vote vote = voteDp.getVote();

            // 장소 이름
            String posName = vote.getPosName();

            // 인기 투표 타입과 투표수
            Map.Entry<String, Long> answerTypeAndCnt = popularCnt(vote);

            bottomResult.add(MPVoteRes.of(vote.getId(), vote.getCreatedAt(), posName, answerTypeAndCnt.getKey(),
                    answerTypeAndCnt.getValue()));
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

}
