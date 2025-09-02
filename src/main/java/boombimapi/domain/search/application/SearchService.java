package boombimapi.domain.search.application;

import boombimapi.domain.search.presentation.dto.res.SearchHistoryRes;
import boombimapi.domain.search.presentation.dto.res.SearchRelatedRes;
import boombimapi.domain.search.presentation.dto.res.SearchRes;

import java.util.List;

public interface SearchService {

    // 최근 검색 내역 10개 조회
    List<SearchHistoryRes> getSearchHistory(String userId);

    // 연관 검색어 조회
    List<SearchRelatedRes> getSearchRelated(String posName);


    // 검색 결과 조회 및 검색 내역 저장
    List<SearchRes> getSearch(String posName);
}
