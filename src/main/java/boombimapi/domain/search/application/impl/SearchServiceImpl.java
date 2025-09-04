package boombimapi.domain.search.application.impl;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.congestion.entity.OfficialCongestion;
import boombimapi.domain.congestion.repository.MemberCongestionRepository;
import boombimapi.domain.congestion.repository.OfficialCongestionRepository;
import boombimapi.domain.favorite.entity.Favorite;
import boombimapi.domain.favorite.repository.FavoriteRepository;
import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.member.domain.repository.MemberRepository;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.entity.OfficialPlace;
import boombimapi.domain.place.entity.PlaceType;
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
    private final FavoriteRepository favoriteRepository;

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
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        if (!Objects.equals(posName, "")) {
            saveSearchWord(posName, member); // 공백은 저장 X
        }


        Pageable limit10 = PageRequest.of(0, 10);

        List<MemberPlace> memberPlaceEntities =
                memberPlaceRepository.findEntitiesByNameContainingIgnoreCase(posName, limit10);

        List<OfficialPlace> officialEntities =
                officialPlaceRepository.findEntitiesByNameContainingIgnoreCase(posName, limit10);


        return sortedCongestion(memberPlaceEntities, officialEntities, member);

    }

    @Override
    public void deletePersonal(Long searchId, String userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        Search search = searchRepository.findById(searchId).orElse(null);
        if (search == null) throw new BoombimException(ErrorCode.SEARCH_NOT_EXISTS);

        if (Objects.equals(search.getMember().getId(), member.getId())) {
            searchRepository.delete(search); // 이중 체크까지 해주자
        } else {
            throw new BoombimException(ErrorCode.SEARCH_NOT_EXISTS);
        }

    }

    @Override
    public void deleteAll(String userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BoombimException(ErrorCode.USER_NOT_EXIST));

        searchRepository.deleteByMember(member);
    }


    private void saveSearchWord(String posName, Member member) {

        Search findSearch = searchRepository.findBySearchWord(posName).orElse(null);
        if (findSearch == null) {
            Search search = Search.builder().member(member).searchWord(posName).build();
            searchRepository.save(search);
        }
    }

    // 장소마다 혼잡도 조회해서 정보 넘겨줘야됨
    private List<SearchRes> sortedCongestion(List<MemberPlace> memberPlaceEntities, List<OfficialPlace> officialEntities, Member member) {
        // 서버에 있는 장소들이 매개변수로 넘어옴

        List<SearchRes> result = new ArrayList<>();

        log.info(String.valueOf(memberPlaceEntities.size()));

        for (MemberPlace memberPlace : memberPlaceEntities) {

            boolean favoriteFlag = false;

            // 해당 장소 즐겨찾기 여부
            Favorite favorite = favoriteRepository.findByMemberAndPlaceIdAndPlaceType(member, memberPlace.getId(), PlaceType.MEMBER_PLACE).orElse(null);
            if (favorite != null) favoriteFlag = true;

            // 혼잡도 정보 때문에 즉 붐빔 키워드랑 최신 반영 날짜 때문에
            MemberCongestion latestMember =
                    memberCongestionRepository.findTop1ByMemberPlaceIdOrderByCreatedAtDesc(memberPlace.getId()).orElse(null);

            if (latestMember != null) {
                result.add(SearchRes.of(memberPlace.getId(), memberPlace.getName(), latestMember.getCreatedAt(),
                        latestMember.getCongestionLevel().getName(), memberPlace.getAddress(), memberPlace.getImageUrl(), PlaceType.MEMBER_PLACE, favoriteFlag));
            }else{
                result.add(SearchRes.of(memberPlace.getId(), memberPlace.getName(), memberPlace.getCreatedAt(),
                        "여유", memberPlace.getAddress(), memberPlace.getImageUrl(), PlaceType.MEMBER_PLACE, favoriteFlag));
            }
        }


        for (OfficialPlace official : officialEntities) {


            boolean favoriteFlag = false;

            // 해당 장소 즐겨찾기 여부
            Favorite favorite = favoriteRepository.findByMemberAndPlaceIdAndPlaceType(member, official.getId(), PlaceType.OFFICIAL_PLACE).orElse(null);
            if (favorite != null) favoriteFlag = true;

            OfficialCongestion latestOfficial =
                    officialCongestionRepository.findTopByOfficialPlaceIdOrderByObservedAtDesc(official.getId()).orElse(null);

            // 혼잡도 정보 때문에 즉 붐빔 키워드랑 최신 반영 날짜 때문에
            if (latestOfficial != null) {
                result.add(SearchRes.of(official.getId(), official.getName(), latestOfficial.getObservedAt(),
                        latestOfficial.getCongestionLevel().getName(), null, official.getImageUrl(), PlaceType.OFFICIAL_PLACE, favoriteFlag));
            }
        }

        result.sort(Comparator.comparing(SearchRes::timeAt).reversed());

        return result;
    }
}
