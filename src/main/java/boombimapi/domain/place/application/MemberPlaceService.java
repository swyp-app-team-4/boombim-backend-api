package boombimapi.domain.place.application;

import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.response.ResolveMemberPlaceResponse;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final MemberPlaceRepository memberPlaceRepository;

    public ResolveMemberPlaceResponse resolveMemberPlace(
        ResolveMemberPlaceRequest request
    ) {
        log.info("[MemberPlaceService] resolveMemberPlace()");

        // TODO: 레이스 컨디션 처리 필요

        MemberPlace memberPlace = memberPlaceRepository.findByUuid(request.uuid())
            .orElseGet(() -> memberPlaceRepository.save(
                MemberPlace.of(
                    request.uuid(),
                    request.latitude(),
                    request.longitude()
                )
            ));

        return ResolveMemberPlaceResponse.from(memberPlace);
    }

}
