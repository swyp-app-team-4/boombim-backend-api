package boombimapi.domain.favorite.application.validator;

import boombimapi.domain.favorite.entity.FavoriteType;

public interface PlaceValidator {

    FavoriteType supports();

    void validate(
        Long placeId
    );

}
