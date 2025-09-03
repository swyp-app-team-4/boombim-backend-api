package boombimapi.domain.favorite.application;

import static boombimapi.domain.place.entity.PlaceType.*;
import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.favorite.application.validator.PlaceValidator;
import boombimapi.domain.favorite.dto.request.AddFavoriteRequest;
import boombimapi.domain.favorite.dto.response.AddFavoriteResponse;
import boombimapi.domain.favorite.dto.response.GetFavoriteResponse;
import boombimapi.domain.favorite.entity.Favorite;
import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.entity.PlaceType;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.domain.place.repository.OfficialPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberPlaceRepository memberPlaceRepository;
    private final OfficialPlaceRepository officialPlaceRepository;
    private final MemberCongestionRepository memberCongestionRepository;
    private final OfficialCongestionRepository officialCongestionRepository;
    private final Map<PlaceType, PlaceValidator> validatorMap;

    public AddFavoriteResponse addFavorite(
        String memberId,
        AddFavoriteRequest request
    ) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        boolean isExists = favoriteRepository.existsByMemberIdAndPlaceIdAndPlaceType(
            memberId,
            request.placeId(),
            request.placeType()
        );

        if (isExists) {
            throw new BoombimException(FAVORITE_ALREADY_EXISTS);
        }

        validatorMap.get(request.placeType()).validate(request.placeId());

        Favorite favorite = favoriteRepository.save(
            Favorite.of(
                member,
                request.placeId(),
                request.placeType()
            )
        );

        return AddFavoriteResponse.from(favorite.getId());
    }

    @Transactional
    public void deleteFavorite(
        String memberId,
        Long placeId,
        PlaceType placeType
    ) {
        favoriteRepository.deleteByMemberIdAndPlaceIdAndPlaceType(
            memberId,
            placeId,
            placeType
        );
    }

    public List<GetFavoriteResponse> getFavoritesWithLatestCongestion(String memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        List<Favorite> favorites = favoriteRepository.findAllByMemberId(member.getId());
        if (favorites.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<GetFavoriteResponse> result = new ArrayList<>(favorites.size());

        for (Favorite favorite : favorites) {
            if (favorite.getPlaceType() == MEMBER_PLACE) {
                handleMemberPlaceFavorite(favorite, now, result);
            } else if (favorite.getPlaceType() == OFFICIAL_PLACE) {
                handleOfficialPlaceFavorite(favorite, result);
            }
        }

        return result;
    }

    private void handleMemberPlaceFavorite(
        Favorite favorite,
        LocalDateTime now,
        List<GetFavoriteResponse> result
    ) {
        MemberPlace memberPlace = memberPlaceRepository.findById(favorite.getPlaceId())
            .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        Optional<MemberCongestion> optionalMemberCongestion = memberCongestionRepository
            .findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(memberPlace.getId(), now);

        optionalMemberCongestion.ifPresent(latest -> result.add(
            GetFavoriteResponse.of(
                favorite.getId(),
                memberPlace.getId(),
                MEMBER_PLACE,
                memberPlace.getLatitude(),
                memberPlace.getLongitude(),
                memberPlace.getName(),
                latest.getCongestionLevel().getName(),
                latest.getCongestionMessage(),
                latest.getCreatedAt()
            )
        ));
    }

    private void handleOfficialPlaceFavorite(
        Favorite favorite,
        List<GetFavoriteResponse> result
    ) {
        Optional<OfficialPlace> optionalOfficialPlace =
            officialPlaceRepository.findById(favorite.getPlaceId());

        if (optionalOfficialPlace.isPresent()) {
            OfficialPlace officialPlace = optionalOfficialPlace.get();

            Optional<OfficialCongestion> optionalOfficialCongestion = officialCongestionRepository
                .findTopByOfficialPlaceIdOrderByObservedAtDesc(officialPlace.getId());

            optionalOfficialCongestion.ifPresent(latest -> result.add(
                GetFavoriteResponse.of(
                    favorite.getId(),
                    officialPlace.getId(),
                    OFFICIAL_PLACE,
                    officialPlace.getCentroidLatitude(),
                    officialPlace.getCentroidLongitude(),
                    officialPlace.getName(),
                    latest.getCongestionLevel().getName(),
                    latest.getCongestionLevel().getMessage(),
                    latest.getObservedAt()
                )
            ));
        }
    }


}
