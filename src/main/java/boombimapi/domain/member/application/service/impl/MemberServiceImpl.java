package boombimapi.domain.member.application.service.impl;


import boombimapi.domain.member.application.service.MemberService;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.res.GetMemberRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository userRepository;


    @Override
    public GetMemberRes getMember(String userId) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        return GetMemberRes.of(user);
    }

    @Override
    public void updateNickname(String userId, String name) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        member.updateName(name);
    }


}
