package boombimapi.domain.member.application.service;


import boombimapi.domain.member.presentation.dto.member.req.MemberLeaveReq;
import boombimapi.domain.member.presentation.dto.member.res.*;

import boombimapi.domain.member.presentation.dto.member.res.mypage.GetMemberResV1;
import boombimapi.domain.member.presentation.dto.member.res.mypage.GetMemberResV2;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MemberServiceV1 {

    // 1번 구간
    GetMemberResV1 getMember(String userId);


    // 3번 구간
    List<MyPageVoteRes> getMyVoteAnswer(String userId);

    // 4번 구간
    List<MyPageVoteRes> getMyVoteQuestion(String userId);

    void updateNickname(String userId, String name);

    GetNicknameRes getNameFlag(String userId);

    // 회원 탈퇴
    void memberDelete(String userId, MemberLeaveReq req);

    // 프로필 수정
    ProfileRes updateProfile(String userId, MultipartFile multipartFile) throws IOException;


}