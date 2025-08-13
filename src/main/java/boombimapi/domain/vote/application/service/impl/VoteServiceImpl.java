package boombimapi.domain.vote.application.service.impl;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.user.domain.repository.UserRepository;
import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import boombimapi.domain.vote.domain.repository.VoteAnswerRepository;
import boombimapi.domain.vote.domain.repository.VoteDuplicationRepository;
import boombimapi.domain.vote.domain.repository.VoteRepository;
import boombimapi.domain.vote.presentation.dto.req.VoteAnswerReq;
import boombimapi.domain.vote.presentation.dto.req.VoteDeleteReq;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import boombimapi.domain.vote.presentation.dto.res.VoteListRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteAnswerRepository voteAnswerRepository;

    private final VoteRepository voteRepository;

    private final VoteDuplicationRepository voteDuplicationRepository;

    private final UserRepository userRepository;

    @Override
    public void registerVote(String userId, VoteRegisterReq req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        //위도 경도 500m 맞는지 true면 있음 false면 없음
        boolean result = isWithin500Meters(
                req.posLatitude(), req.posLongitude(),
                req.userLatitude(), req.userLongitude()
        );
        if (!result) throw new BoombimException(ErrorCode.OUT_OF_500M_RADIUS);


        // 중복 검사인지 확인
        Vote vote = voteRepository.findByPosId(req.posId()).orElse(null);

        // 중복이면 덮어 씌어야됨
        if (vote != null) {
            // 1. 같은 유저가 본인꺼 또 등록하려고 할떄 덮어씌우면 안되고 그냥 에러
            if (user.getId().equals(vote.getUser().getId())) {
                throw new BoombimException(ErrorCode.DUPLICATE_POS_ID);
            }

            // 2. 다른 사용자면 종속으로 저장!
            VoteDuplication vd = VoteDuplication.builder().vote(vote).user(user).build();
            voteDuplicationRepository.save(vd);
            throw new BoombimException(ErrorCode.DUPLICATE_POS_ID);
        }

        Vote vb = Vote.builder()
                .user(user)
                .posId(req.posId())
                .posName(req.posName())
                .latitude(req.posLatitude())
                .longitude(req.posLongitude()).build();
        voteRepository.save(vb);


        //1. 지역 누르면 투표 생성 api  -
        // 중복 검사인지 확인해야됨
        // 타이머는 30분 이때 다른 사용자가 똑같은거하면 덮어쓰기 이해되지??
        // 또한 위도 경도 맞게

    }

    @Override
    public void answerVote(String userId, VoteAnswerReq req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        Vote vote = voteRepository.findById(req.voteId()).orElse(null);
        if (vote == null) throw new BoombimException(ErrorCode.VOTE_NOT_EXIST);


        // 같은 투표 중복자 막기
        VoteAnswer voteAnswer = voteAnswerRepository.findByUserAndVote(user, vote).orElse(null);
        if (voteAnswer != null) throw new BoombimException(ErrorCode.DUPLICATE_VOTE_USER);

        // 투표 완료
        voteAnswerRepository.save(VoteAnswer.builder()
                .user(user)
                .vote(vote)
                .answerType(req.voteAnswerType()).build());

        // =======
        // 여기서 혼잡도 정보한테도 넘겨야됨 이건 추후!!
        // =======

    }

    @Override
    public void deleteVote(String userId, VoteDeleteReq req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        Vote vote = voteRepository.findById(req.voteId()).orElse(null);
        if (vote == null) throw new BoombimException(ErrorCode.VOTE_NOT_EXIST);

        // 다른사용자가 눌렀을떄 혹시 모르니깐!!
        if(!Objects.equals(vote.getUser().getId(), user.getId())) throw new BoombimException(ErrorCode.NO_PERMISSION_TO_CLOSE_VOTE);

        // 투표 종료 비활성화 false로 바꿈
        vote.updateIsVoteDeactivate();
    }

    @Override
    public VoteListRes listVote(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        // 사용자 위치에 맞게 떠야됨 근데 이거 나중에


        //중복도 내투표에 뜨게해야된다.!


        return null;
    }


    // 허버사인 공식 500m 반경 파악
    public boolean isWithin500Meters(double posLatitude, double posLongitude,
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

        return distance <= 500; // 500m 이내면 true
    }

}
