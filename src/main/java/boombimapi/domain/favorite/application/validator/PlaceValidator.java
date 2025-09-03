package boombimapi.domain.favorite.application.validator;

import boombimapi.domain.place.entity.PlaceType;

public interface PlaceValidator {

    PlaceType supports();

    void validate(
        Long placeId
    );

}
