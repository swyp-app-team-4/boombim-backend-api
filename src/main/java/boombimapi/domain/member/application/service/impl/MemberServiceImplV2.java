package boombimapi.domain.member.application.service.impl;


import boombimapi.domain.alarm.application.service.FcmService;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.member.application.service.MemberServiceV2;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.entity.MemberLeave;
import boombimapi.domain.member.domain.repository.MemberLeaveRepository;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.member.presentation.dto.member.req.MemberLeaveReq;
import boombimapi.domain.member.presentation.dto.member.res.*;
import boombimapi.domain.member.presentation.dto.member.res.mypage.GetCongestionHistoryRes;
import boombimapi.domain.member.presentation.dto.member.res.mypage.GetMemberResV2;
import boombimapi.domain.place.command.entity.MemberPlace;
import boombimapi.domain.place.command.repository.MemberPlaceRepository;
import boombimapi.domain.point.domain.entity.Point;
import boombimapi.domain.point.domain.repository.PointRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.s3.presentation.application.S3Service;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static boombimapi.global.infra.exception.error.ErrorCode.POINT_NOT_EXIST;
import static boombimapi.global.infra.exception.error.ErrorCode.USER_NOT_EXIST;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberServiceImplV2 implements MemberServiceV2 {

    private final MemberRepository userRepository;
    private final S3Service s3Service;
    private final MemberLeaveRepository memberLeaveRepository;
    private final FcmService fcmService;
    private final PointRepository pointRepository;
    private final MemberCongestionRepository memberCongestionRepository;
    private final MemberPlaceRepository memberPlaceRepository;

    @Override
    public GetMemberResV2 getMember(String userId) {
        Member member = userRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        Point point = pointRepository.findByMember(member)
                .orElseThrow(() -> new BoombimException(POINT_NOT_EXIST));

        return GetMemberResV2.of(member, point.getBalance());
    }


    @Override
    public void updateNickname(String userId, String name) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) {
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);
        }

        member.updateName(name);
    }

    @Override
    public void memberDelete(String userId, MemberLeaveReq req) {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) {
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);
        }

        fcmService.deleteFcmToken(userId);
        memberLeaveRepository.save(MemberLeave.builder().leaveReason(req.leaveReason()).build());

        userRepository.delete(member);
    }

    @Override
    public ProfileRes updateProfile(String userId, MultipartFile multipartFile) throws IOException {
        Member member = userRepository.findById(userId).orElse(null);
        if (member == null) {
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);
        }

        String profile = s3Service.storeUserProFile(multipartFile, userId);
        member.updateProfile(profile);

        return ProfileRes.of(profile);
    }

    @Override
    public List<GetCongestionHistoryRes> getUserCongestionHistory(String memberId) {
        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        List<MemberCongestion> allMemberCongestion =
                memberCongestionRepository.findAllByMemberOrderByCreatedAtDesc(member);

        List<GetCongestionHistoryRes> result = new ArrayList<>();

        for (MemberCongestion memberCongestion : allMemberCongestion) {
            result.add(GetCongestionHistoryRes.of(memberCongestion.getMemberPlace().getName(), memberCongestion));
        }

        return result;
    }


}
