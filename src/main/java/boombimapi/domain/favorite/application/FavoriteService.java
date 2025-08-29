package boombimapi.domain.favorite.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.favorite.dto.request.AddFavoriteRequest;
import boombimapi.domain.favorite.dto.response.AddFavoriteResponse;
import boombimapi.domain.favorite.dto.response.GetFavoriteResponse;
import boombimapi.domain.favorite.entity.Favorite;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final MemberCongestionRepository memberCongestionRepository;

    public AddFavoriteResponse addFavorite(
        String memberId,
        AddFavoriteRequest request
    ) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        MemberPlace memberPlace = memberPlaceRepository.findById(request.memberPlaceId())
            .orElseThrow(() -> new BoombimException(MEMBER_PLACE_NOT_FOUND));

        if (favoriteRepository.existsByMemberIdAndMemberPlaceId(member.getId(), memberPlace.getId())) {
            throw new BoombimException(FAVORITE_ALREADY_EXISTS);
        }

        Favorite favorite = favoriteRepository.save(
            Favorite.of(
                member,
                memberPlace
            )
        );

        return AddFavoriteResponse.from(favorite.getId());
    }

    @Transactional
    public void deleteFavorite(
        String memberId,
        Long memberPlaceId
    ) {
        favoriteRepository.deleteByMemberIdAndMemberPlaceId(
            memberId, memberPlaceId
        );
    }

    public List<GetFavoriteResponse> getFavoritesWithLatestCongestion(
        String memberId
    ) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BoombimException(USER_NOT_EXIST));

        List<Favorite> favorites = favoriteRepository
            .findAllByMemberId(member.getId());

        if (favorites.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<GetFavoriteResponse> result = new ArrayList<>(favorites.size());

        for (Favorite favorite : favorites) {
            MemberPlace memberPlace = favorite.getMemberPlace();

            Optional<MemberCongestion> optionalMemberCongestion = memberCongestionRepository
                .findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
                    memberPlace.getId(), now
                );

            optionalMemberCongestion.ifPresent(
                latestMemberCongestion -> result.add(
                    GetFavoriteResponse.of(
                        favorite.getId(),
                        memberPlace.getId(),
                        memberPlace.getLatitude(),
                        memberPlace.getLongitude(),
                        memberPlace.getName(),
                        latestMemberCongestion.getCongestionLevel().getName(),
                        latestMemberCongestion.getCongestionMessage(),
                        latestMemberCongestion.getCreatedAt()
                    )
                ));
        }

        return result;
    }

}
