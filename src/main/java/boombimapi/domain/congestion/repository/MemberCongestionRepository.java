package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.MemberCongestion;
import boombimapi.domain.place.entity.MemberPlace;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberCongestionRepository extends JpaRepository<MemberCongestion, Long> {

    boolean existsByMemberPlaceIdAndExpiresAtAfter(
        Long memberPlaceId,
        LocalDateTime now
    );

    Optional<MemberCongestion> findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
        Long memberPlaceId,
        LocalDateTime now
    );

    @Query("""
           SELECT COUNT(mc)
           FROM MemberCongestion mc
           WHERE mc.memberPlace.id = :placeId
             AND CAST(mc.createdAt AS DATE) = CURRENT_DATE
           """)
    long countTodayByPlace(@Param("placeId") Long placeId);

    Optional<MemberCongestion> findTop1ByMemberPlaceIdOrderByCreatedAtDesc(Long placeId);

    Slice<MemberCongestion> findByMemberPlaceIdOrderByIdDesc(
        Long memberPlaceId,
        Pageable pageable
    );

    Slice<MemberCongestion> findByMemberPlaceIdAndIdLessThanOrderByIdDesc(
        Long memberPlaceId,
        Long cursor,
        Pageable pageable
    );

    @Query("""
        select mc
        from MemberCongestion mc
        join fetch mc.congestionLevel cl
        where mc.memberPlace = :place
        order by mc.createdAt desc
        """)
    List<MemberCongestion> findLatestByPlaceFetchLevel(
        @Param("place") MemberPlace place,
        Pageable pageable
    );

}
