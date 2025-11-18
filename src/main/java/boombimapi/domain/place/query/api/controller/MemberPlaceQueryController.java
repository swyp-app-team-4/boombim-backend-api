package boombimapi.domain.place.query.api.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.cluster.Clusterer;
import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.marker.ViewportMarkerResponse;
import boombimapi.domain.place.query.service.MemberPlaceQueryService;
import boombimapi.global.properties.AppClusterProperties;
import boombimapi.global.properties.WebClusterProperties;
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
public class MemberPlaceQueryController {

    private final MemberPlaceQueryService memberPlaceQueryService;

    private final Clusterer webClusterer;
    private final Clusterer appClusterer;

    private final WebClusterProperties webClusterProperties;
    private final AppClusterProperties appClusterProperties;

    @Operation(summary = "[APP] 뷰포트 내 사용자 장소 조회", description = "뷰포트 내 사용자 장소들 중 1시간 내 작성된 혼잡도가 존재하는 장소들을 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 사용자 장소 조회 성공")
    })
    @PostMapping({"/app/public/member-place", "/app/member-place"})
    public ResponseEntity<BaseResponse<List<ViewportMarkerResponse>>> getMemberPlacesInViewportApp(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        List<ViewportMarkerResponse> memberPlacesInViewport = memberPlaceQueryService.getMemberPlacesInViewport(
            memberId,
            request,
            appClusterer,
            appClusterProperties
        );

        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_MEMBER_PLACES_IN_VIEWPORT_SUCCESS,
                memberPlacesInViewport
            )
        );
    }

    @Operation(summary = "[Web] 뷰포트 내 사용자 장소 조회", description = "뷰포트 내 사용자 장소들 중 1시간 내 작성된 혼잡도가 존재하는 장소들을 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 사용자 장소 조회 성공")
    })
    @PostMapping("/web/public/member-place")
    public ResponseEntity<BaseResponse<List<ViewportMarkerResponse>>> getMemberPlacesInViewportWeb(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        List<ViewportMarkerResponse> viewportMarkers = memberPlaceQueryService.getMemberPlacesInViewport(
            memberId,
            request,
            webClusterer,
            webClusterProperties
        );

        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_MEMBER_PLACES_IN_VIEWPORT_SUCCESS,
                viewportMarkers
            )
        );
    }

}
