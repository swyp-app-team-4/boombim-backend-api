package boombimapi.domain.member.application.service.impl;


import boombimapi.domain.member.application.service.MemberService;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.res.GetNicknameRes;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return GetMemberRes.of(user, (long) (voteDus.size() + votes.size()), (long) voteAnswers.size());
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


}
