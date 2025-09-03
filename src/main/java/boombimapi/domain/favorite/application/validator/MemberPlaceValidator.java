package boombimapi.domain.favorite.application.validator;

import boombimapi.domain.favorite.entity.FavoriteType;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberPlaceValidator implements PlaceValidator {

    private final MemberPlaceRepository memberPlaceRepository;

    @Override
    public FavoriteType supports() {
        return FavoriteType.MEMBER_PLACE;
    }

    @Override
    public void validate(Long placeId) {
        memberPlaceRepository.findById(placeId)
            .orElseThrow(() -> new BoombimException(ErrorCode.MEMBER_PLACE_NOT_FOUND));
    }
}
