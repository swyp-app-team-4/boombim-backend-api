package boombimapi.domain.place.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.application.MemberPlaceService;
import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.member.GetMemberPlaceDetailResponse;
import boombimapi.domain.place.dto.response.member.ResolveMemberPlaceResponse;
import boombimapi.domain.place.dto.response.node.ViewportNodeResponse;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member-place")
@Tag(name = "Member Place", description = "사용자 장소 관련 API")
public class MemberPlaceController {

    private final MemberPlaceService memberPlaceService;

    @Operation(summary = "사용자 장소 등록 및 등록 여부 확인", description = "사용자가 혼잡도를 등록하려는 장소가 이미 서버 단에 저장이 되어있는지 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 장소 확인 성공")
    })
    @PostMapping("/resolve")
    public ResponseEntity<BaseResponse<ResolveMemberPlaceResponse>> resolveMemberPlace(
        @RequestBody ResolveMemberPlaceRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                RESOLVE_MEMBER_PLACE_SUCCESS,
                memberPlaceService.resolveMemberPlace(request)
            )
        );
    }

    @Operation(summary = "뷰포트 내 사용자 장소 조회", description = "뷰포트 내 사용자 장소들 중 1시간 내 작성된 혼잡도가 존재하는 장소들을 리스트로 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뷰포트 내 사용자 장소 조회 성공")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<List<ViewportNodeResponse>>> getMemberPlacesInViewport(
        @AuthenticationPrincipal String memberId,
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_MEMBER_PLACES_IN_VIEWPORT_SUCCESS,
                memberPlaceService.getViewportNodes(memberId, request)
            )
        );
    }

    @Operation(summary = "특정 사용자 장소 상세 조회", description = "특정 사용자 장소를 상세 조회하여 해당 장소에 작성된 혼잡도들을 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "특정 사용자 장소 상세 조회 성공")
    })
    @GetMapping("/{memberPlaceId}")
    public ResponseEntity<BaseResponse<GetMemberPlaceDetailResponse>> getMemberPlaceDetail(
        @PathVariable Long memberPlaceId,
        @RequestParam(required = false) @Min(1) @Max(100) Integer size,
        @RequestParam(required = false) Long cursor,
        @AuthenticationPrincipal String memberId
    ) {

        GetMemberPlaceDetailResponse memberPlaceDetailResponse = memberPlaceService.getMemberPlaceDetail(
            memberId,
            memberPlaceId,
            size,
            cursor
        );

        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_MEMBER_PLACE_DETAIL_SUCCESS,
                memberPlaceDetailResponse
            )
        );

    }

}
