package boombimapi.domain.favorite.application;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.favorite.dto.request.AddFavoriteRequest;
import boombimapi.domain.favorite.dto.response.AddFavoriteResponse;
import boombimapi.domain.favorite.entity.Favorite;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberPlaceRepository memberPlaceRepository;

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

    public void deleteFavorite(
        String memberId,
        Long memberPlaceId
    ) {
        favoriteRepository.deleteByMemberIdAndMemberPlaceId(
            memberId, memberPlaceId
        );
    }

}
