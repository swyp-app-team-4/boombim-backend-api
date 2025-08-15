package boombimapi.domain.place.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.application.OfficialPlaceService;
import boombimapi.domain.place.dto.request.ViewportRequest;
import boombimapi.domain.place.dto.response.ViewportResponse;
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
@RequestMapping("/official-places")
public class OfficialPlaceController {

    private final OfficialPlaceService officialPlaceService;

    @PostMapping
    public ResponseEntity<BaseResponse<List<ViewportResponse>>> getOfficialPlacesInViewport(
        @RequestBody ViewportRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_IN_VIEWPORT_SUCCESS,
                officialPlaceService.getOfficialPlacesInViewport(request)
            )
        );
    }

}
