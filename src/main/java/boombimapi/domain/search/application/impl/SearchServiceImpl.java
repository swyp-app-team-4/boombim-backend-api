package boombimapi.domain.search.application.impl;

import boombimapi.domain.search.application.SearchService;
import boombimapi.domain.search.domain.repository.SearchRepository;
import boombimapi.domain.search.presentation.dto.res.SearchHistoryRes;
import boombimapi.domain.search.presentation.dto.res.SearchRelatedRes;
import boombimapi.domain.search.presentation.dto.res.SearchRes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;

    @Override
    public List<SearchHistoryRes> getSearchHistory(String userId) {
        return null;
    }

    @Override
    public List<SearchRelatedRes> getSearchRelated(String posName) {
        return null;
    }

    @Override
    public List<SearchRes> getSearch(String posName) {
        return null;
    }
}
