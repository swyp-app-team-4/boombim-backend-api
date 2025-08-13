package boombimapi.domain.place.official.query.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.official.query.service.OfficialPlaceQueryService;
import boombimapi.domain.place.official.query.dto.request.ViewportRequest;
import boombimapi.domain.place.official.query.dto.response.MapMarkerResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/official-place")
@Tag(name = "OfficialPlace", description = "공식 장소 API")
public class OfficialPlaceQueryController {

    private final OfficialPlaceQueryService officialPlaceQueryService;

    @Operation(summary = "뷰포트 내 공식 장소 마커 조회 API", description = "뷰포트 내에 위치한 공식 장소들의 좌표를 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "공식 장소 마커 조회 성공")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<List<MapMarkerResponse>>> getMarkersInViewport(
        @RequestBody ViewportRequest viewportRequest
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_WITHIN_VIEWPORT_SUCCESS,
                officialPlaceQueryService.getMarkersInViewport(viewportRequest)
            )
        );
    }


}
