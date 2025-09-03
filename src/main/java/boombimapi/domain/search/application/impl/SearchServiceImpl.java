package boombimapi.domain.search.application.impl;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.repository.MemberPlaceRepository;
import boombimapi.domain.place.repository.OfficialPlaceRepository;
import boombimapi.domain.search.application.SearchService;
import boombimapi.domain.search.domain.entity.Search;
import boombimapi.domain.search.domain.repository.SearchRepository;
import boombimapi.domain.search.presentation.dto.PlaceNameProjection;
import boombimapi.domain.search.presentation.dto.res.SearchHistoryRes;
import boombimapi.domain.search.presentation.dto.res.SearchRelatedRes;
import boombimapi.domain.search.presentation.dto.res.SearchRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;

    private final MemberRepository memberRepository;

    // 장소
    private final MemberPlaceRepository memberPlaceRepository;
    private final OfficialPlaceRepository officialPlaceRepository;

    // 혼잡도
    private final OfficialCongestionRepository officialCongestionRepository;
    private final MemberCongestionRepository memberCongestionRepository;

    @Override
    public List<SearchHistoryRes> getSearchHistory(String userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));


        return searchRepository.findByMember(member)
                .stream()
                .map(search -> SearchHistoryRes.of(search.getId(), search.getSearchWord()))
                .toList();
    }

    @Override
    public List<SearchRelatedRes> getSearchRelated(String posName) {
        // 각 소스에서 10개씩만 가져오고, 합쳐서 중복 제거 → 최종 10개
        Pageable limit10 = PageRequest.of(0, 10);

        List<PlaceNameProjection> fromMember =
                memberPlaceRepository.searchByName(posName, limit10);

        List<PlaceNameProjection> fromOfficial =
                officialPlaceRepository.searchByName(posName, limit10);

        // 이름 기준으로 중복 제거 (대소문자 무시), 입력 키워드와의 "접두" 우선 정렬 → 그다음 사전식
        String keyLower = posName.toLowerCase(Locale.ROOT);

        // 주석 설명 상세히 해놓음!
        return Stream.concat(fromMember.stream(), fromOfficial.stream()) // MemberPlace, OfficialPlace 검색 결과 두 개 합침
                .map(PlaceNameProjection::getName) // Projection 객체에서 placeName(String)만 추출
                .filter(Objects::nonNull) // null 값 제거
                .collect(Collectors.toCollection(LinkedHashSet::new)) // 혹시 모르니 중복 제거(Set) + 순서 유지(LinkedHashSet)
                .stream() // 다시 스트림으로 변환
                .sorted((a, b) -> { // 정렬 기준 정의
                    String la = a.toLowerCase(Locale.ROOT); // a 문자열 소문자 변환 (Locale 영향 없음)
                    String lb = b.toLowerCase(Locale.ROOT); // b 문자열 소문자 변환
                    boolean aStarts = la.startsWith(keyLower); // a가 검색 키워드로 시작하는지
                    boolean bStarts = lb.startsWith(keyLower); // b가 검색 키워드로 시작하는지
                    if (aStarts && !bStarts) return -1; // a만 키워드로 시작하면 a를 먼저
                    if (!aStarts && bStarts) return 1;  // b만 키워드로 시작하면 b를 먼저
                    return la.compareTo(lb); // 둘 다 시작 or 둘 다 아니면 사전순 정렬
                })
                .limit(10) // 최대 10개까지만 가져오기
                .map(SearchRelatedRes::of) // String → DTO(SearchRelatedRes) 변환
                .toList(); // 최종적으로 List<SearchRelatedRes> 반환

    }

    @Override
    public List<SearchRes> getSearch(String posName, String userId) {
        if(!Objects.equals(posName, "")){
            saveSearchWord(posName, userId); // 공백은 저장 X
        }


        Pageable limit10 = PageRequest.of(0, 10);

        List<MemberPlace> memberPlaceEntities =
                memberPlaceRepository.findEntitiesByNameContainingIgnoreCase(posName, limit10);

        List<OfficialPlace> officialEntities =
                officialPlaceRepository.findEntitiesByNameContainingIgnoreCase(posName, limit10);


        return sortedCongestion(memberPlaceEntities, officialEntities);

    }


    private void saveSearchWord(String posName, String userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        Search findSearch = searchRepository.findBySearchWord(posName).orElse(null);
        if (findSearch == null) {
            Search search = Search.builder().member(member).searchWord(posName).build();
            searchRepository.save(search);
        }
    }

    // 장소마다 혼잡도 조회해서 정보 넘겨줘야됨
    private List<SearchRes> sortedCongestion(List<MemberPlace> memberPlaceEntities, List<OfficialPlace> officialEntities) {
        List<SearchRes> result = new ArrayList<>();

        for (MemberPlace memberPlace : memberPlaceEntities) {

            MemberCongestion latestMember =
                    memberCongestionRepository.findTop1ByMemberPlaceIdOrderByCreatedAtDesc(memberPlace.getId()).orElse(null);

            if (latestMember != null) {


                result.add(SearchRes.of(memberPlace.getId(), memberPlace.getName(), latestMember.getCreatedAt(),
                        latestMember.getCongestionLevel().getName(), "주소", memberPlace.getImageUrl()));
            }
        }


        for (OfficialPlace official : officialEntities) {
            OfficialCongestion latestOfficial =
                    officialCongestionRepository.findTopByOfficialPlaceIdOrderByObservedAtDesc(official.getId()).orElse(null);
            if (latestOfficial != null) {
                result.add(SearchRes.of(official.getId(), official.getName(), latestOfficial.getObservedAt(),
                        latestOfficial.getCongestionLevel().getName(), "주소", official.getImageUrl()));
            }
        }

        result.sort(Comparator.comparing(SearchRes::timeAt).reversed());

        return result;
    }
}
