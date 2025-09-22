package boombimapi.domain.vote.application.service.impl;

import boombimapi.domain.congestion.application.MemberCongestionService;
import boombimapi.domain.congestion.dto.request.CreateMemberCongestionRequest;
import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.repository.CongestionLevelRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.command.service.MemberPlaceService;
import boombimapi.domain.place.command.api.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.command.api.dto.response.ResolveMemberPlaceResponse;
import boombimapi.domain.place.command.entity.MemberPlace;
import boombimapi.domain.place.command.repository.MemberPlaceRepository;
import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import boombimapi.domain.vote.domain.entity.type.VoteStatus;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import boombimapi.domain.vote.presentation.dto.res.list.MyVoteRes;
import boombimapi.domain.vote.presentation.dto.res.list.VoteRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.feignclient.naver.NaverImageClient;
import boombimapi.global.infra.feignclient.naver.dto.res.NaverImageSearchRes;
import feign.FeignException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static boombimapi.global.infra.exception.error.ErrorCode.MEMBER_PLACE_NOT_FOUND;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteAnswerRepository voteAnswerRepository;

    private final VoteRepository voteRepository;

    private final VoteDuplicationRepository voteDuplicationRepository;

    private final MemberRepository userRepository;

    private final NaverImageClient naverImageClient;

    private final MemberCongestionService memberCongestionService;

    private final MemberPlaceService memberPlaceService;

    private final MemberPlaceRepository memberPlaceRepository;

    private final CongestionLevelRepository congestionLevelRepository;

    // 투표 등록
    @Override
    @Transactional(noRollbackFor = BoombimException.class)
    public void registerVote(String userId, VoteRegisterReq req) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        //위도 경도 100m 맞는지 true면 있음 false면 없음 ==> 이제부터 300m로 통일 == > 이거 기능 날라감 ㅜㅜㅜ
        /**
         *         boolean result = isWithin300Meters(
         *                 req.posLatitude(), req.posLongitude(),
         *                 req.userLatitude(), req.userLongitude()
         *         );
         *         if (!result) throw new BoombimException(ErrorCode.OUT_OF_300M_RADIUS);
         *
         * */


        // 중복 검사인지 확인
        Vote vote = voteRepository.findByPosIdAndIsVoteActivateTrue(req.posId()).orElse(null);

        // 중복이면 덮어 씌어야됨
        if (vote != null) {
            // 1. 활성화 된거면 안됨
            // 2. 같은 유저가 본인꺼 또 등록하려고 할떄 덮어씌우면 안되고 그냥 에러
            if (user.getId().equals(vote.getMember().getId())) {
                throw new BoombimException(ErrorCode.DUPLICATE_USER);
            }

            // 3. 다른 사용자고 종속 저장 했는데 또 하면 저장 안되게
            List<Member> usersByVote = voteDuplicationRepository.findMembersByVote(vote);
            for (Member userD : usersByVote) {
                if (user.getId().equals(userD.getId()))
                    throw new BoombimException(ErrorCode.DUPLICATE_USER);
            }

            // 4. 다른 사용자고 처음이면 종속으로 저장!
            VoteDuplication vd = VoteDuplication.builder().vote(vote).member(user).build();
            voteDuplicationRepository.save(vd);
            throw new BoombimException(ErrorCode.DUPLICATE_POS_ID);
        }

        // String posImage = getPosImage(req.posName());

        String posImage = null;

        // 공식 장소 테이블 추가
        ResolveMemberPlaceResponse resolveMemberPlaceResponse =
                memberPlaceService
                        .resolveMemberPlace(
                                ResolveMemberPlaceRequest.of(
                                        req.posId(),
                                        req.posName(),
                                        req.address(),
                                        req.posLatitude(),
                                        req.posLongitude(),
                                        posImage));
        MemberPlace memberPlace = memberPlaceRepository.findById(resolveMemberPlaceResponse.memberPlaceId())
                .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        Vote vb = Vote.builder()
                .member(user)
                .memberPlace(memberPlace)
                .posId(req.posId())
                .posImage("https://gamehackspring.s3.ap-northeast-2.amazonaws.com/appicon.png")
                .posName(req.posName())
                .latitude(req.posLatitude())
                .longitude(req.posLongitude()).build();
        voteRepository.save(vb);

        vb.updateEndTime(30);

        //1. 지역 누르면 투표 생성 api  -
        // 중복 검사인지 확인해야됨
        // 타이머는 30분 이때 다른 사용자가 똑같은거하면 덮어쓰기 이해되지??
        // 또한 위도 경도 맞게

    }

    // 투표하기
    @Override
    public void answerVote(String userId, VoteAnswerReq req) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        Vote vote = voteRepository.findById(req.voteId()).orElse(null);
        if (vote == null) throw new BoombimException(ErrorCode.VOTE_NOT_EXIST);

        // 투표 종료됐는데 투표할려고 할때
        if (!vote.isVoteActivate()) throw new BoombimException(ErrorCode.VOTE_ALREADY_CLOSED);

        // 본인 투표 막기 즉 본인이 올린거 투표 못하게 해야됨
        if(vote.getMember().getId().equals(user.getId())) throw new BoombimException(ErrorCode.SELF_VOTE_NOT_ALLOWED);

        // voteDuplication 도 막기 이것보 본인이 합류한거니깐 막아야됨
        List<VoteDuplication> voteDuplications = vote.getVoteDuplications();
        for (VoteDuplication voteDuplication : voteDuplications) {
            if(voteDuplication.getVote().getId().equals(vote.getId())){
                throw new BoombimException(ErrorCode.SELF_VOTE_NOT_ALLOWED);
            }
        }

        // 같은 투표 중복자 막기
        VoteAnswer voteAnswer = voteAnswerRepository.findByMemberAndVote(user, vote).orElse(null);
        if (voteAnswer != null) throw new BoombimException(ErrorCode.DUPLICATE_VOTE_USER);

        // 투표 완료
        voteAnswerRepository.save(VoteAnswer.builder()
                .member(user)
                .vote(vote)
                .answerType(req.voteAnswerType()).build());

        // =======
        // 여기서 혼잡도 정보한테도 넘겨야됨
        createCongestion(vote, req.voteAnswerType(), userId);
        // =======

    }

    // 투표 종료
    @Override
    public void endVote(String userId, VoteDeleteReq req) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null)
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        Vote vote = voteRepository.findById(req.voteId()).orElse(null);
        if (vote == null)
            throw new BoombimException(ErrorCode.VOTE_NOT_EXIST);

        // 다른사용자가 눌렀을떄 혹시 모르니깐!!
        if (!Objects.equals(vote.getMember().getId(), user.getId())) {
            throw new BoombimException(ErrorCode.NO_PERMISSION_TO_CLOSE_VOTE);
        }
        // 투표 종료 비활성화 false로 바꿈
        vote.updateIsVoteDeactivate();
        vote.updateStatusDeactivate();

        // 투표 알림 가게 true로 변환
        vote.updatePassivityAlarmActivate();

    }

    // 투표 리스트들
    @Override
    public VoteListRes listVote(String userId, double latitude, double longitude) {
        Member user = userRepository.findById(userId).orElse(null);
        if (user == null)
            throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        // 투표 리스트(사용자 위치에 맞게 떠야됨)
        List<VoteRes> voteRes = voteList(user, latitude, longitude);

        // 내 질문 투표 리스트
        List<MyVoteRes> myVoteRes = myVoteList(user);

        return VoteListRes.of(voteRes, myVoteRes);
    }

    private List<VoteRes> voteList(Member user, double latitude, double longitude) {

        List<VoteRes> voteResList = new ArrayList<>();

        /**
         * QA 떄문에 잠시 닫겠음
         */
        List<Vote> votes = calculate300(latitude, longitude);

        /**
         * QA 끝나면 주석 처리 할 예정
         */
        // List<Vote> votes = voteRepository.findAll();

        for (Vote vote : votes) {
            if (!vote.isVoteActivate() || vote.getVoteStatus().equals(VoteStatus.END))
                continue;

            List<Long> voteAnswer = voteAnswerCnt(vote);
            boolean voteFlag = voteUsercheck(vote, user);

            voteResList.add(
                    VoteRes.of(vote.getId(), profileTopThree(vote), (long) vote.getVoteDuplications().size(), vote.getCreatedAt(), vote.getPosName(),
                            vote.getPosImage(),
                            voteAnswer.get(0), voteAnswer.get(1), voteAnswer.get(2), voteAnswer.get(3), "투표하기", voteFlag));
        }

        return voteResList;
    }

    private List<MyVoteRes> myVoteList(Member user) {
        List<MyVoteRes> myVoteRes = new ArrayList<>();
        ///  내꺼 가져오기
        List<Vote> myVoteList = voteRepository.findByMember(user);
        for (Vote vote : myVoteList) {
            //// 각 투표마다 투표자들 가져오기
            List<Long> voteAnswer = voteAnswerCnt(vote);
            boolean voteFlag = voteUsercheck(vote, user);
            myVoteRes.add(
                    MyVoteRes.of(vote.getId(), profileTopThree(vote), (long) vote.getVoteDuplications().size(), vote.getCreatedAt(), vote.getPosName(),
                            voteAnswer.get(0), voteAnswer.get(1), voteAnswer.get(2), voteAnswer.get(3),
                            "내 질문", vote.getVoteStatus(), voteFlag));
        }

        /// 투표 중복꺼 가져오기 즉 중속
        List<VoteDuplication> duplicationMyList = voteDuplicationRepository.findByMember(user);
        for (VoteDuplication voteDuplication : duplicationMyList) {
            Vote vote = voteRepository.findById(voteDuplication.getVote().getId()).orElse(null);
            if (vote == null)
                throw new BoombimException(ErrorCode.VOTE_NOT_EXIST);

            List<Long> voteAnswer = voteAnswerCnt(vote);
            boolean voteFlag = voteUsercheck(vote, user);
            myVoteRes.add(
                    MyVoteRes.of(vote.getId(), profileTopThree(vote), (long) vote.getVoteDuplications().size(), vote.getCreatedAt(), vote.getPosName(),
                            voteAnswer.get(0), voteAnswer.get(1), voteAnswer.get(2), voteAnswer.get(3), "내 질문", vote.getVoteStatus(), voteFlag));
        }

        return myVoteRes;
    }

    // 허버사인 공식 500m 반경 파악
    public boolean isWithin300Meters(double posLatitude, double posLongitude,
                                     double userLatitude, double userLongitude) {

        final double EARTH_RADIUS = 6371000; // 지구 반지름 (m)

        // 위도, 경도 차이 → 라디안 변환
        double dLat = Math.toRadians(userLatitude - posLatitude);
        double dLon = Math.toRadians(userLongitude - posLongitude);

        // Haversine 공식
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(posLatitude)) * Math.cos(Math.toRadians(userLatitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c; // 두 점 사이 거리(m)

        return distance <= 300; // 500m 이내면 true
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


    private List<Vote> calculate300(double latitude, double longitude) {
        final double RADIUS_M = 300.0;

        // 1) 바운딩 박스(사각형) 계산: 위도 1도 ≈ 111,320m, 경도 1도 ≈ 111,320 * cos(lat)
        double latDelta = RADIUS_M / 111_320d;
        double lonDelta = RADIUS_M / (111_320d * Math.cos(Math.toRadians(latitude)));

        double latMin = latitude - latDelta;
        double latMax = latitude + latDelta;
        double lonMin = longitude - lonDelta;
        double lonMax = longitude + lonDelta;

        // 2) 후보 조회 (바운딩 박스 안만)
        //   - 만약 아래 메서드가 없으면 findAll()로 받고 스트림에서 lat/lon 사각형 필터 먼저 해도 됨.
        List<Vote> candidates = voteRepository.findAllInBoundingBox(latMin, latMax, lonMin, lonMax);

        // 3) 하버사인으로 500m 이내만 남기고, 거리 기준 정렬
        List<Vote> within300m = candidates.stream()
                .filter(v -> distanceMeters(latitude, longitude, v.getLatitude(), v.getLongitude()) <= RADIUS_M)
                .sorted(Comparator.comparingDouble(v -> distanceMeters(latitude, longitude, v.getLatitude(), v.getLongitude())))
                .toList();

        return within300m;
    }


    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000d; // 지구 반지름(m)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private boolean voteUsercheck(Vote vote, Member user) {
        boolean voteFlag = false;
        List<VoteAnswer> voteAnswers = vote.getVoteAnswers();
        for (VoteAnswer voteAnswer : voteAnswers) {
            if (Objects.equals(voteAnswer.getMember().getId(), user.getId())) {
                return true;
            }
        }

        return voteFlag;
    }


    // 상위 3건 유저 프로필 이미지 링크
    public List<String> profileTopThree(Vote vote) {
        return vote.getVoteDuplications().stream()
                .map(voteAnswer -> voteAnswer.getMember().getProfile()) // Member의 프로필 URL 추출
                .filter(Objects::nonNull)                               // null 값 제거 (안전)
                .limit(3)                                               // 최대 3개만
                .toList();
    }

    private String getPosImage(String posName) {
        try {
            // 네이버 이미지 검색 API 호출
            NaverImageSearchRes response = naverImageClient.searchImages(
                    posName + " 전경",   // 검색어 보정 (예: "부평남초등학교 전경")
                    10,                 // 여러 개 가져오기
                    1,
                    "sim",              // 정확도순
                    "large"             // 큰 이미지 우선
            );

            if (response.items() == null || response.items().isEmpty()) {
                log.warn("🔍 이미지 검색 결과 없음: {}", posName);
                return "https://gamehackspring.s3.ap-northeast-2.amazonaws.com/appicon.png";
            }

            // 후보 중에서 "급식/식단/메뉴" 같은 거 제외하고 첫 번째 반환
            return response.items().stream()
                    .filter(item -> !item.title().contains("급식"))
                    .filter(item -> !item.title().contains("식단"))
                    .filter(item -> !item.title().contains("메뉴"))
                    .filter(item -> !item.title().contains("사람"))
                    .findFirst()
                    .map(NaverImageSearchRes.Item::link)   // DTO 맞게 수정
                    .orElse(response.items().get(0).link());

        } catch (FeignException e) {
            log.error("❌ 네이버 이미지 API 호출 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private void createCongestion(Vote vote, VoteAnswerType answerType, String userId) {

        String displayName = answerType.getDisplayName();

        CongestionLevel congestionLevel = congestionLevelRepository.findByName(displayName).orElse(null);
        if (congestionLevel == null)
            throw new BoombimException(ErrorCode.CONGESTION_LEVEL_NOT_FOUND);

        memberCongestionService.createMemberCongestion(userId,
                CreateMemberCongestionRequest.of(vote.getMemberPlace().getId(), congestionLevel.getId(), "", vote.getLatitude(), vote.getLongitude()));
    }


}