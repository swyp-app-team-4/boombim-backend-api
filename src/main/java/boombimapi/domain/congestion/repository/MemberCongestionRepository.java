package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.MemberCongestion;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCongestionRepository extends JpaRepository<MemberCongestion, Long> {

    boolean existsByMemberPlaceIdAndExpiresAtAfter(
        Long memberPlaceId,
        LocalDateTime now
    );

    Optional<MemberCongestion> findFirstByMemberPlaceIdAndExpiresAtAfterOrderByCreatedAtDesc(
        Long memberPlaceId,
        LocalDateTime now
    );

}
