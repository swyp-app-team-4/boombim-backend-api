package boombimapi.domain.congestion.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.dto.request.CreateMemberCongestionRequest;
import boombimapi.domain.congestion.dto.response.CreateMemberCongestionResponse;
import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.CongestionLevelRepository;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCongestionService {

    private final MemberRepository memberRepository;
    private final MemberPlaceRepository memberPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;
    private final CongestionLevelRepository congestionLevelRepository;

    public CreateMemberCongestionResponse createMemberCongestion(
        String memberId,
        CreateMemberCongestionRequest request
    ) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        MemberPlace memberPlace = memberPlaceRepository.findById(request.memberPlaceId())
            .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        CongestionLevel congestionLevel = congestionLevelRepository.findById(request.congestionLevelId())
            .orElseThrow(() -> new BoombimException(CONGESTION_LEVEL_NOT_FOUND));

        String congestionMessage = request.congestionMessage();
        if (congestionMessage.isEmpty()) {
            congestionMessage = congestionLevel.getMessage();
        }

        MemberCongestion memberCongestion = MemberCongestion.of(
            member,
            memberPlace,
            congestionLevel,
            congestionMessage,
            request.latitude(),
            request.longitude()
        );

        return CreateMemberCongestionResponse
            .from(memberCongestionRepository.save(memberCongestion));
    }

}
