package boombimapi.domain.member.application.service;


import boombimapi.domain.member.presentation.dto.res.GetMemberRes;

public interface MemberService {

    GetMemberRes getMember(String userId);

}