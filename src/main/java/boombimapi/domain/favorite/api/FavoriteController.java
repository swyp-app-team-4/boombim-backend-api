package boombimapi.domain.favorite.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.favorite.application.FavoriteService;
import boombimapi.domain.favorite.dto.request.AddFavoriteRequest;
import boombimapi.domain.favorite.dto.response.AddFavoriteResponse;
import boombimapi.domain.favorite.dto.response.GetFavoriteResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite")
@Tag(name = "Favorite", description = "사용자 즐겨찾기 관련 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 추가", description = "해당 장소를 즐겨찾기에 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공")
    })
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

    @Operation(summary = "즐겨찾기 삭제", description = "해당 장소를 즐겨찾기에서 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "즐겨찾기 삭제 성공")
    })
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

    @Operation(summary = "즐겨찾기 조회", description = "사용자가 즐겨찾기한 장소들의 최신 혼잡도를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 즐겨찾기 최신 혼잡도 조회 성공")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<GetFavoriteResponse>>> getFavorites(
        @AuthenticationPrincipal String memberId
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_FAVORITES_SUCCESS,
                favoriteService.getFavoritesWithLatestCongestion(memberId)
            )
        );
    }

}
