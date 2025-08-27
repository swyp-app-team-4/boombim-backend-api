package boombimapi.domain.region.presentation.controller;

import boombimapi.domain.region.application.service.RegionService;
import boombimapi.domain.region.presentation.dto.res.RegionRes;
import boombimapi.domain.vote.application.service.VoteService;
import boombimapi.domain.vote.presentation.dto.req.VoteRegisterReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/region")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Region", description = "지역 소식 관련 API")
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역 소식 데이터 조회", description = "지역 소식 API를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "투표 생성 성공"),
    })
    @GetMapping
    public ResponseEntity<List<RegionRes>> getRegion(@RequestParam("date") LocalDate date) {
        return ResponseEntity.ok(regionService.getRegion(date));
    }
}