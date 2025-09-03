package boombimapi.domain.search.presentation.controller;

import boombimapi.domain.search.application.SearchService;
import boombimapi.domain.search.presentation.dto.res.SearchHistoryRes;
import boombimapi.domain.search.presentation.dto.res.SearchRelatedRes;
import boombimapi.domain.search.presentation.dto.res.SearchRes;
import boombimapi.global.response.BaseOKResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @Operation(description = "검색 내역 조회 10개만")
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistoryRes>> getSearchHistory(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(searchService.getSearchHistory(userId));
    }

    @Operation(description = "연관검색어")
    @GetMapping("/related")
    public ResponseEntity<List<SearchRelatedRes>> getSearchRelated(@RequestParam String posName) {
        return ResponseEntity.ok(searchService.getSearchRelated(posName));
    }

    @Operation(description = "검색 결과 및 검색 내용 저장")
    @GetMapping
    public ResponseEntity<List<SearchRes>> getSearch(
            @RequestParam String posName,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(searchService.getSearch(posName, userId));
    }

    @Operation(description = "개인 삭제")
    @DeleteMapping
    public ResponseEntity<BaseOKResponse<Void>> deletePersonal(@RequestParam String posName) {
        return null;
    }

    @Operation(description = "전체 삭제")
    @DeleteMapping("/all")
    public ResponseEntity<BaseOKResponse<Void>> deleteAll(@RequestParam String posName) {
        return null;
    }

}
