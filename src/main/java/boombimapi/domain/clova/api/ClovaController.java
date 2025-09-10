package boombimapi.domain.clova.api;

import static boombimapi.global.response.ResponseMessage.*;

import boombimapi.domain.clova.application.ClovaService;
import boombimapi.domain.clova.dto.request.GenerateCongestionMessageRequest;
import boombimapi.domain.clova.dto.response.GenerateCongestionMessageResponse;
import boombimapi.global.response.BaseResponse;
import boombimapi.global.response.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clova")
@Tag(name = "Clova", description = "NAVER CLOVA Studio 관련 API")
public class ClovaController {

    private final ClovaService clovaService;

    @Operation(summary = "혼잡도 메시지 자동 완성", description = "사용자 혼잡도 생성 시 혼잡도 메시지를 CLOVA Studio로 자동 완성시킵니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 혼잡도 메시지 생성 성공")
    })
    @PostMapping("/congestion-message")
    public ResponseEntity<BaseResponse<GenerateCongestionMessageResponse>> generateCongestionMessage(
        @RequestBody GenerateCongestionMessageRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                GENERATE_CONGESTION_MESSAGE_SUCCESS,
                clovaService.generateCongestionMessage(request)
            )
        );
    }

}
