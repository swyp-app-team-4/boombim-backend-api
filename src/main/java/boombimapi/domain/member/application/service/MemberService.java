package boombimapi.domain.member.application.service;


import boombimapi.domain.member.presentation.dto.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.res.GetNicknameRes;

import boombimapi.domain.member.presentation.dto.res.MyPageVoteRes;

import java.util.List;

public interface MemberService {

    // 1번 구간
    GetMemberRes getMember(String userId);

    // 3번 구간
    List<MyPageVoteRes> getMyVoteAnswer(String userId);

    // 4번 구간
    List<MyPageVoteRes> getMyVote(String userId);

    void updateNickname(String userId, String name);

    GetNicknameRes getNameFlag(String userId);

}