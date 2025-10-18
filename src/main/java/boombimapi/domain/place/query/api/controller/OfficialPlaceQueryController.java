package boombimapi.domain.place.query.api.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.ViewportResponse;
import boombimapi.domain.place.query.service.OfficialPlaceQueryService;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OfficialPlaceQueryController {

    private final OfficialPlaceQueryService officialPlaceQueryService;

    @Operation(summary = "뷰포트 내 공식 장소 조회", description = "뷰포트 내 공식 장소들의 정보를 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 공식 장소 조회 성공")
    })
    @PostMapping("/app/official-place")
    public ResponseEntity<BaseResponse<List<ViewportResponse>>> getOfficialPlacesInViewportApp(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS,
                officialPlaceQueryService.getOfficialPlacesInViewport(memberId, request)
            )
        );
    }

    @Operation(summary = "뷰포트 내 공식 장소 조회", description = "뷰포트 내 공식 장소들의 정보를 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 공식 장소 조회 성공")
    })
    @PostMapping("/web/official-place")
    public ResponseEntity<BaseResponse<List<ViewportResponse>>> getOfficialPlacesInViewportWeb(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS,
                officialPlaceQueryService.getOfficialPlacesInViewport(memberId, request)
            )
        );
    }


}
