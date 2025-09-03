package boombimapi.domain.search.presentation.controller;

import boombimapi.domain.search.application.SearchService;
import boombimapi.domain.search.presentation.dto.req.DeletePersonalReq;
import boombimapi.domain.search.presentation.dto.res.SearchHistoryRes;
import boombimapi.domain.search.presentation.dto.res.SearchRelatedRes;
import boombimapi.domain.search.presentation.dto.res.SearchRes;
import boombimapi.global.response.BaseOKResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static boombimapi.global.response.ResponseMessage.DELETE_SEARCH_SUCCESS;
import static boombimapi.global.response.ResponseMessage.GET_ALARM_SUCCESS;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search", description = "검색 관련 API")
public class SearchController {

    private final SearchService searchService;
    @Operation(summary = "검색 내역 조회", description = "사용자가 입력한 검색 내역 조회합니다. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 내역 조회 성공"),
    })
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistoryRes>> getSearchHistory(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(searchService.getSearchHistory(userId));
    }

    @Operation(summary = "연관 검색어", description = "연관 검색어가 나옵니다. 최대 20개까지 나옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "연관 검색어 조회 성공"),
    })
    @GetMapping("/related")
    public ResponseEntity<List<SearchRelatedRes>> getSearchRelated(@RequestParam String posName) {
        return ResponseEntity.ok(searchService.getSearchRelated(posName));
    }

    @Operation(summary = "검색 상세 조회", description = "검색 버튼을 누르면 실행되는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 상세 조회 성공"),
    })
    @GetMapping
    public ResponseEntity<List<SearchRes>> getSearch(
            @RequestParam String posName,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(searchService.getSearch(posName, userId));
    }

    @Operation(summary = "검색 내역 개별 삭제", description = "검색 내역을 개별 삭제를 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "검색 ID 존재하지가 않음"),
    })
    @DeleteMapping("/{searchId}")
    public ResponseEntity<BaseOKResponse<Void>> deletePersonal(
            @AuthenticationPrincipal String userId,
            @PathVariable Long searchId) {
        searchService.deletePersonal(searchId, userId);
        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        DELETE_SEARCH_SUCCESS));
    }

    @Operation(summary = "검색 내역 전체 삭제", description = "검색 내역을 전체를 삭제를 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 전체 삭제 성공"),
    })
    @DeleteMapping("/all")
    public ResponseEntity<BaseOKResponse<Void>> deleteAll(@AuthenticationPrincipal String userId) {

        searchService.deleteAll(userId);
        return ResponseEntity.ok(
                BaseOKResponse.of(
                        HttpStatus.OK,
                        DELETE_SEARCH_SUCCESS));
    }

}
