package boombimapi.domain.favorite.application.validator;

import boombimapi.domain.place.entity.PlaceType;
import boombimapi.domain.place.repository.OfficialPlaceRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfficialPlaceValidator implements PlaceValidator {

    private final OfficialPlaceRepository officialPlaceRepository;

    @Override
    public PlaceType supports() {
        return PlaceType.OFFICIAL_PLACE;
    }

    @Override
    public void validate(Long placeId) {
        officialPlaceRepository.findById(placeId)
            .orElseThrow(() -> new BoombimException(ErrorCode.OFFICIAL_PLACE_NOT_FOUND));
    }
}
