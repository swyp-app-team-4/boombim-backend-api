package boombimapi.domain.clova.api;

import boombimapi.domain.clova.application.ClovaService;
import boombimapi.domain.clova.dto.request.GenerateCongestionMessageRequest;
import boombimapi.domain.clova.dto.response.GenerateCongestionMessageResponse;
import boombimapi.global.response.BaseResponse;
import boombimapi.global.response.ResponseMessage;
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
public class ClovaController {

    private final ClovaService clovaService;

    @PostMapping("/congestion-message")
    public ResponseEntity<BaseResponse<GenerateCongestionMessageResponse>> generateCongestionMessage(
        @RequestBody GenerateCongestionMessageRequest request
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(
                HttpStatus.OK,
                ResponseMessage.GENERATE_CONGESTION_MESSAGE_SUCCESS,
                clovaService.generateCongestionMessage(request)
            )
        );
    }

}
