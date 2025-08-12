package boombimapi.domain.congestion.official.query.controller;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.congestion.official.query.dto.OfficialCongestionResponse;
import boombimapi.domain.congestion.official.query.service.OfficialCongestionQueryService;
import boombimapi.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/official-congestion")
@Tag(name = "OfficialCongestion", description = "공식 혼잡도 API")
public class OfficialCongestionQueryController {

    private final OfficialCongestionQueryService officialCongestionQueryService;

    @Operation(summary = "공식 장소의 최신 공식 혼잡도 조회 API", description = "특정 공식 장소의 ID로 최신 공식 혼잡도를 반환합니다.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "최신 공식 혼잡도 조회 성공")
        }
    )
    @GetMapping("/{officialPlaceId}/latest")
    public ResponseEntity<BaseResponse<OfficialCongestionResponse>> getLatestOfficialCongestion(
        @PathVariable Long officialPlaceId
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GET_LATEST_OFFICIAL_CONGESTION_SUCCESS,
                officialCongestionQueryService.getLatestOfficialCongestion(officialPlaceId)
            )
        );
    }

}
