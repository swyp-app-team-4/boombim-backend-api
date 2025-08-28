package boombimapi.domain.favorite.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.favorite.application.FavoriteService;
import boombimapi.domain.favorite.dto.request.AddFavoriteRequest;
import boombimapi.domain.favorite.dto.response.AddFavoriteResponse;
import boombimapi.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<BaseResponse<AddFavoriteResponse>> addFavorite(
        @AuthenticationPrincipal String memberId,
        @RequestBody AddFavoriteRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                ADD_FAVORITE_SUCCESS,
                favoriteService.addFavorite(memberId, request)
            )
        );
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteFavorite(
        @AuthenticationPrincipal String memberId,
        @RequestParam Long memberPlaceId
    ) {
        favoriteService.deleteFavorite(memberId, memberPlaceId);

        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                DELETE_FAVORITE_SUCCESS,
                null
            )
        );
    }

}
