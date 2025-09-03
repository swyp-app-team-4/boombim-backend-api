package boombimapi.domain.place.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.application.OfficialPlaceService;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.official.OfficialPlaceOverviewResponse;
import boombimapi.domain.place.dto.response.ViewportResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/official-place")
@Tag(name = "Official Place", description = "공식 장소 관련 API")
public class OfficialPlaceController {

    private final OfficialPlaceService officialPlaceService;

    @Operation(summary = "뷰포트 내 공식 장소 조회", description = "뷰포트 내 공식 장소들의 정보를 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 공식 장소 조회 성공")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<List<ViewportResponse>>> getOfficialPlacesInViewport(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS,
                officialPlaceService.getOfficialPlacesInViewport(memberId, request)
            )
        );
    }

    @Operation(summary = "특정 공식 장소 정보 조회", description = "특정 공식 장소의 인구 통계 및 예상 혼잡도를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "특정 공식 장소의 정보 조회 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 공식 장소"),
        @ApiResponse(responseCode = "401", description = "존재하지 않는 공식 혼잡도 정보")
    })
    @GetMapping("/{officialPlaceId}/overview")
    public ResponseEntity<BaseResponse<OfficialPlaceOverviewResponse>> getOfficialPlaceOverview(
        @AuthenticationPrincipal String memberId,
        @PathVariable Long officialPlaceId
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACE_OVERVIEW_SUCCESS,
                officialPlaceService.getOverview(memberId, officialPlaceId)
            )
        );
    }

}
