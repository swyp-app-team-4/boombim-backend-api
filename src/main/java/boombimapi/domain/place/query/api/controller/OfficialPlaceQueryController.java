package boombimapi.domain.place.query.api.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.query.api.dto.request.ViewportRequest;
import boombimapi.domain.place.query.api.dto.response.ViewportResponse;
import boombimapi.domain.place.query.service.OfficialPlaceQueryService;
import boombimapi.global.response.BaseResponse;
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
@RequestMapping("/query/official-place")
public class OfficialPlaceQueryController {

    private final OfficialPlaceQueryService officialPlaceQueryService;

    @PostMapping
    public ResponseEntity<BaseResponse<List<ViewportResponse>>> getOfficialPlacesInViewport(
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
