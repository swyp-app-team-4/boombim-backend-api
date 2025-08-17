package boombimapi.domain.member.application.service;


import boombimapi.domain.member.presentation.dto.res.GetMemberRes;
import boombimapi.domain.member.presentation.dto.res.GetNicknameRes;

public interface MemberService {

    GetMemberRes getMember(String userId);

    void updateNickname(String userId, String name);

    GetNicknameRes getNameFlag(String userId);

}