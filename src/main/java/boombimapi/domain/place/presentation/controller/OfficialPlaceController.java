package boombimapi.domain.place.presentation.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.place.application.service.impl.OfficialPlaceServiceImpl;
import boombimapi.domain.place.presentation.dto.request.Viewport;
import boombimapi.domain.place.presentation.dto.response.MapMarker;
import boombimapi.global.response.ApiResponse;
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
public class OfficialPlaceController {

    private final OfficialPlaceServiceImpl officialPlaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<MapMarker>>> getMarkersInViewport(
        @RequestBody Viewport viewport
    ) {
        return ResponseEntity.ok(
            ApiResponse.of(
                HttpStatus.OK,
                GET_OFFICIAL_PLACES_WITHIN_VIEWPORT_SUCCESS,
                officialPlaceService.getMarkersInViewport(viewport)
            )
        );
    }


}
