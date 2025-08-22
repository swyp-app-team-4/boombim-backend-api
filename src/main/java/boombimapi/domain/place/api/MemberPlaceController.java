package boombimapi.domain.place.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.application.MemberPlaceService;
import boombimapi.domain.place.dto.request.ResolveMemberPlaceRequest;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.ResolveMemberPlaceResponse;
import boombimapi.domain.place.dto.response.ViewportNodeResponse;
import boombimapi.global.response.BaseResponse;
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
@RequestMapping("/member-place")
public class MemberPlaceController {

    private final MemberPlaceService memberPlaceService;

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

    @PostMapping
    public ResponseEntity<BaseResponse<List<ViewportNodeResponse>>> getMemberPlacesInViewport(
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_MEMBER_PLACES_IN_VIEWPORT_SUCCESS,
                memberPlaceService.getViewportNodes(request)
            )
        );
    }

//    @GetMapping("/{memberPlaceId}")
//    public ResponseEntity<BaseResponse<>>

}
