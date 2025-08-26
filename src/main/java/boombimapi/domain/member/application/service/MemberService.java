package boombimapi.domain.member.application.service;


import boombimapi.domain.member.presentation.dto.member.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.member.res.GetNicknameRes;

import boombimapi.domain.member.presentation.dto.member.res.MyPageVoteRes;

import java.util.List;

public interface MemberService {

    // 1번 구간
    GetMemberRes getMember(String userId);

    // 3번 구간
    List<MyPageVoteRes> getMyVoteAnswer(String userId);

    // 4번 구간
    List<MyPageVoteRes> getMyVoteQuestion(String userId);

    void updateNickname(String userId, String name);

    GetNicknameRes getNameFlag(String userId);

    // 회원 탈퇴
    void memberDelete(String userId);


}