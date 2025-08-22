package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.MemberCongestion;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCongestionRepository extends JpaRepository<MemberCongestion, Long> {

    boolean existsByMemberPlaceIdAndExpiresAtAfter(
        Long memberPlaceId,
        Instant now
    );

    Optional<MemberCongestion> findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
        Long memberPlaceId,
        Instant now
    );

}
