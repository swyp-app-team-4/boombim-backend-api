package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.MemberCongestion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import boombimapi.domain.place.entity.MemberPlace;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
           SELECT COUNT(mc)\s
           FROM MemberCongestion mc
           WHERE mc.memberPlace.id = :placeId
             AND CAST(mc.createdAt AS DATE) = CURRENT_DATE
           """)
    long countTodayByPlace(@Param("placeId") Long placeId);


    Optional<MemberCongestion> findTop1ByMemberPlaceIdOrderByCreatedAtDesc(Long placeId);


}
